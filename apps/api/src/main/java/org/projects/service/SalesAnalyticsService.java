package org.projects.service;

import org.projects.analytics.SalesFact;
import org.projects.analytics.SalesSummary;
import org.projects.analytics.SalesSummaryCalculator;
import org.projects.dto.HourlySalesResponse;
import org.projects.dto.SalesSummaryResponse;
import org.projects.dto.TopProductResponse;
import org.projects.persistence.entity.SalesRecordEntity;
import org.projects.repository.ImportBatchRepository;
import org.projects.repository.SalesRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SalesAnalyticsService {
    private final ImportBatchRepository importBatchRepository;
    private final SalesRecordRepository salesRecordRepository;
    private final SalesSummaryCalculator salesSummaryCalculator;
    private final CurrentStoreContextService currentStoreContextService;

    public SalesAnalyticsService(
        ImportBatchRepository importBatchRepository,
        SalesRecordRepository salesRecordRepository,
        SalesSummaryCalculator salesSummaryCalculator,
        CurrentStoreContextService currentStoreContextService
    ) {
        this.importBatchRepository = importBatchRepository;
        this.salesRecordRepository = salesRecordRepository;
        this.salesSummaryCalculator = salesSummaryCalculator;
        this.currentStoreContextService = currentStoreContextService;
    }

    public SalesSummaryResponse getSalesSummary(UUID batchId) {
        UUID storeId = currentStoreContextService.getCurrentStoreId();
        if (importBatchRepository.findByIdAndStoreId(batchId, storeId).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Import batch not found");
        }

        List<SalesFact> facts = salesRecordRepository.findByImportBatchIdAndStoreIdOrderBySoldAtAsc(batchId, storeId).stream()
            .map(this::toFact)
            .toList();

        SalesSummary summary = salesSummaryCalculator.summarize(facts);
        return new SalesSummaryResponse(
            summary.revenue(),
            summary.unitsSold(),
            summary.receipts(),
            summary.averageBasket(),
            summary.topProducts().stream()
                .map(product -> new TopProductResponse(product.productName(), product.unitsSold()))
                .toList(),
            summary.hourlyDistribution().stream()
                .map(hour -> new HourlySalesResponse(hour.hour(), hour.revenue()))
                .toList()
        );
    }

    private SalesFact toFact(SalesRecordEntity entity) {
        return new SalesFact(
            entity.getReceiptId(),
            entity.getSoldAt(),
            entity.getProductSku(),
            entity.getProductName(),
            entity.getQuantity(),
            entity.getTotalAmount()
        );
    }
}
