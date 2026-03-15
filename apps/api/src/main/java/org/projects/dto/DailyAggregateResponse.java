package org.projects.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DailyAggregateResponse(
    UUID storeId,
    LocalDate businessDate,
    BigDecimal revenue,
    int unitsSold,
    int receipts,
    BigDecimal averageBasket,
    String topProductName,
    Integer topProductUnits
) {
}
