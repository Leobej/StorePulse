package org.projects.service;

import org.projects.analytics.AlertCandidate;
import org.projects.analytics.AnalyticsRuleSet;
import org.projects.analytics.DailySalesAggregate;
import org.projects.analytics.DailySalesAnalyticsResult;
import org.projects.analytics.DailySalesAnalyticsService;
import org.projects.analytics.SalesFact;
import org.projects.domain.AlertStatus;
import org.projects.events.AlertCreatedEvent;
import org.projects.events.KpiComputedEvent;
import org.projects.eventbus.EventBus;
import org.projects.persistence.entity.AlertRecord;
import org.projects.persistence.entity.SalesDailyAggregateEntity;
import org.projects.persistence.entity.SalesRecordEntity;
import org.projects.persistence.entity.Store;
import org.projects.repository.AlertRecordRepository;
import org.projects.repository.SalesDailyAggregateRepository;
import org.projects.repository.SalesRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalyticsMaterializationService {
    private final SalesRecordRepository salesRecordRepository;
    private final SalesDailyAggregateRepository salesDailyAggregateRepository;
    private final AlertRecordRepository alertRecordRepository;
    private final DailySalesAnalyticsService dailySalesAnalyticsService;
    private final EventBus eventBus;
    private final BigDecimal minDailyRevenue;
    private final BigDecimal minAverageBasket;

    public AnalyticsMaterializationService(
        SalesRecordRepository salesRecordRepository,
        SalesDailyAggregateRepository salesDailyAggregateRepository,
        AlertRecordRepository alertRecordRepository,
        DailySalesAnalyticsService dailySalesAnalyticsService,
        EventBus eventBus,
        @Value("${storepulse.analytics.min-daily-revenue}") BigDecimal minDailyRevenue,
        @Value("${storepulse.analytics.min-average-basket}") BigDecimal minAverageBasket
    ) {
        this.salesRecordRepository = salesRecordRepository;
        this.salesDailyAggregateRepository = salesDailyAggregateRepository;
        this.alertRecordRepository = alertRecordRepository;
        this.dailySalesAnalyticsService = dailySalesAnalyticsService;
        this.eventBus = eventBus;
        this.minDailyRevenue = minDailyRevenue;
        this.minAverageBasket = minAverageBasket;
    }

    @Transactional
    public void recomputeAllDailyAggregates() {
        Map<UUID, List<SalesRecordEntity>> recordsByStore = salesRecordRepository.findAll().stream()
            .collect(Collectors.groupingBy(record -> record.getStore().getId()));

        for (List<SalesRecordEntity> storeRecords : recordsByStore.values()) {
            Store store = storeRecords.getFirst().getStore();
            List<SalesFact> facts = storeRecords.stream()
                .map(this::toFact)
                .toList();

            DailySalesAnalyticsResult result = dailySalesAnalyticsService.analyze(
                facts,
                new AnalyticsRuleSet(minDailyRevenue, minAverageBasket)
            );

            for (DailySalesAggregate aggregate : result.aggregates()) {
                upsertAggregate(store, aggregate);
                eventBus.publish(new KpiComputedEvent(aggregate.businessDate(), java.time.Instant.now()));
            }

            for (AlertCandidate alertCandidate : result.alertCandidates()) {
                createAlertIfMissing(store, alertCandidate);
            }
        }
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

    private void upsertAggregate(Store store, DailySalesAggregate aggregate) {
        OffsetDateTime now = OffsetDateTime.now();
        SalesDailyAggregateEntity entity = salesDailyAggregateRepository.findByStoreIdAndBusinessDate(store.getId(), aggregate.businessDate())
            .orElseGet(() -> new SalesDailyAggregateEntity(
                UUID.randomUUID(),
                aggregate.businessDate(),
                store,
                aggregate.summary().revenue(),
                aggregate.summary().unitsSold(),
                aggregate.summary().receipts(),
                aggregate.summary().averageBasket(),
                null,
                null,
                now,
                now
            ));

        entity.setRevenue(aggregate.summary().revenue());
        entity.setUnitsSold(aggregate.summary().unitsSold());
        entity.setReceipts(aggregate.summary().receipts());
        entity.setAverageBasket(aggregate.summary().averageBasket());
        entity.setStore(store);
        entity.setTopProductName(aggregate.summary().topProducts().isEmpty() ? null : aggregate.summary().topProducts().getFirst().productName());
        entity.setTopProductUnits(aggregate.summary().topProducts().isEmpty() ? null : aggregate.summary().topProducts().getFirst().unitsSold());
        entity.setUpdatedAt(now);
        salesDailyAggregateRepository.save(entity);
    }

    private void createAlertIfMissing(Store store, AlertCandidate alertCandidate) {
        alertRecordRepository.findByStoreIdAndTypeAndBusinessDateAndStatus(store.getId(), alertCandidate.type(), alertCandidate.businessDate(), AlertStatus.OPEN)
            .orElseGet(() -> {
                AlertRecord alert = new AlertRecord(
                    UUID.randomUUID(),
                    alertCandidate.type(),
                    alertCandidate.severity(),
                    AlertStatus.OPEN,
                    store,
                    alertCandidate.businessDate(),
                    alertCandidate.message(),
                    OffsetDateTime.now(),
                    null
                );
                AlertRecord saved = alertRecordRepository.save(alert);
                eventBus.publish(new AlertCreatedEvent(saved.getId(), saved.getType(), saved.getSeverity(), saved.getBusinessDate(), java.time.Instant.now()));
                return saved;
            });
    }
}
