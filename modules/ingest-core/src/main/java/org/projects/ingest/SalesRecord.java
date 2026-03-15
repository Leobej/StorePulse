package org.projects.ingest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SalesRecord(
    String receiptId,
    OffsetDateTime soldAt,
    String productSku,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal totalAmount
) {
}
