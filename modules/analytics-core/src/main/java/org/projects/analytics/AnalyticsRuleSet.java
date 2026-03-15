package org.projects.analytics;

import java.math.BigDecimal;

public record AnalyticsRuleSet(
    BigDecimal minDailyRevenue,
    BigDecimal minAverageBasket
) {
}
