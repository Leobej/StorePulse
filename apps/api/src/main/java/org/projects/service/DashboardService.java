package org.projects.service;

import org.projects.dto.DailyAggregateResponse;
import org.projects.dto.DashboardOverviewResponse;
import org.projects.persistence.entity.SalesDailyAggregateEntity;
import org.projects.repository.SalesDailyAggregateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {
    private final SalesDailyAggregateRepository salesDailyAggregateRepository;
    private final AlertService alertService;
    private final CurrentStoreContextService currentStoreContextService;

    public DashboardService(
        SalesDailyAggregateRepository salesDailyAggregateRepository,
        AlertService alertService,
        CurrentStoreContextService currentStoreContextService
    ) {
        this.salesDailyAggregateRepository = salesDailyAggregateRepository;
        this.alertService = alertService;
        this.currentStoreContextService = currentStoreContextService;
    }

    public DashboardOverviewResponse getOverview() {
        List<DailyAggregateResponse> aggregates = salesDailyAggregateRepository.findAllByStoreIdOrderByBusinessDateDesc(currentStoreContextService.getCurrentStoreId()).stream()
            .map(this::toResponse)
            .toList();
        return new DashboardOverviewResponse(aggregates, alertService.getOpenAlerts());
    }

    private DailyAggregateResponse toResponse(SalesDailyAggregateEntity entity) {
        return new DailyAggregateResponse(
            entity.getStore().getId(),
            entity.getBusinessDate(),
            entity.getRevenue(),
            entity.getUnitsSold(),
            entity.getReceipts(),
            entity.getAverageBasket(),
            entity.getTopProductName(),
            entity.getTopProductUnits()
        );
    }
}
