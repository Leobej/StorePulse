package org.projects.analytics;

import java.math.BigDecimal;

public record HourlySales(
    int hour,
    BigDecimal revenue
) {
}
