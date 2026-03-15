package org.projects.analytics;

import java.math.BigDecimal;
import java.util.List;

public record SalesSummary(
    BigDecimal revenue,
    int unitsSold,
    int receipts,
    BigDecimal averageBasket,
    List<TopProduct> topProducts,
    List<HourlySales> hourlyDistribution
) {
}
