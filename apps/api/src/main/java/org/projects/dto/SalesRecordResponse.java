package org.projects.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SalesRecordResponse(
    UUID id,
    UUID storeId,
    UUID importBatchId,
    String receiptId,
    OffsetDateTime soldAt,
    String productSku,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal totalAmount
) {
}
