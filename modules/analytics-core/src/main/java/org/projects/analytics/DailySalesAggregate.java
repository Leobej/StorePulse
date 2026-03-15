package org.projects.analytics;

import java.time.LocalDate;

public record DailySalesAggregate(
    LocalDate businessDate,
    SalesSummary summary
) {
}
