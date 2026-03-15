package org.projects.dto;

import java.math.BigDecimal;

public record HourlySalesResponse(
    int hour,
    BigDecimal revenue
) {
}
