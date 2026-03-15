package org.projects.service;

import org.projects.dto.ImportRowErrorResponse;
import org.projects.dto.SalesRecordResponse;
import org.projects.persistence.entity.ImportRowError;
import org.projects.persistence.entity.SalesRecordEntity;
import org.projects.repository.ImportBatchRepository;
import org.projects.repository.ImportRowErrorRepository;
import org.projects.repository.SalesRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SalesQueryService {
    private final ImportBatchRepository importBatchRepository;
    private final SalesRecordRepository salesRecordRepository;
    private final ImportRowErrorRepository importRowErrorRepository;
    private final CurrentStoreContextService currentStoreContextService;

    public SalesQueryService(
        ImportBatchRepository importBatchRepository,
        SalesRecordRepository salesRecordRepository,
        ImportRowErrorRepository importRowErrorRepository,
        CurrentStoreContextService currentStoreContextService
    ) {
        this.importBatchRepository = importBatchRepository;
        this.salesRecordRepository = salesRecordRepository;
        this.importRowErrorRepository = importRowErrorRepository;
        this.currentStoreContextService = currentStoreContextService;
    }

    public List<SalesRecordResponse> getSalesRecords(UUID batchId) {
        UUID storeId = currentStoreContextService.getCurrentStoreId();
        assertBatchExists(batchId, storeId);
        return salesRecordRepository.findByImportBatchIdAndStoreIdOrderBySoldAtAsc(batchId, storeId).stream()
            .map(this::toSalesRecordResponse)
            .toList();
    }

    public List<ImportRowErrorResponse> getRowErrors(UUID batchId) {
        assertBatchExists(batchId, currentStoreContextService.getCurrentStoreId());
        return importRowErrorRepository.findByImportBatchIdOrderByLineNumberAsc(batchId).stream()
            .map(this::toImportRowErrorResponse)
            .toList();
    }

    private SalesRecordResponse toSalesRecordResponse(SalesRecordEntity entity) {
        return new SalesRecordResponse(
            entity.getId(),
            entity.getStore().getId(),
            entity.getImportBatch().getId(),
            entity.getReceiptId(),
            entity.getSoldAt(),
            entity.getProductSku(),
            entity.getProductName(),
            entity.getQuantity(),
            entity.getUnitPrice(),
            entity.getTotalAmount()
        );
    }

    private ImportRowErrorResponse toImportRowErrorResponse(ImportRowError entity) {
        return new ImportRowErrorResponse(entity.getLineNumber(), entity.getErrorMessage());
    }

    private void assertBatchExists(UUID batchId, UUID storeId) {
        if (importBatchRepository.findByIdAndStoreId(batchId, storeId).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Import batch not found");
        }
    }
}
