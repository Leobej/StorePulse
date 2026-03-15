package org.projects.analytics;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SalesFact(
    String receiptId,
    OffsetDateTime soldAt,
    String productSku,
    String productName,
    int quantity,
    BigDecimal totalAmount
) {
}
