package org.projects.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesSummaryResponse(
    BigDecimal revenue,
    int unitsSold,
    int receipts,
    BigDecimal averageBasket,
    List<TopProductResponse> topProducts,
    List<HourlySalesResponse> hourlyDistribution
) {
}
