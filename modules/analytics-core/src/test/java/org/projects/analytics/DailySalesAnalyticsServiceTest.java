package org.projects.analytics;

import org.junit.jupiter.api.Test;
import org.projects.domain.AlertType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailySalesAnalyticsServiceTest {
    private final DailySalesAnalyticsService service = new DailySalesAnalyticsService(new SalesSummaryCalculator());

    @Test
    void groupsByDayAndGeneratesAlertCandidates() {
        List<SalesFact> facts = List.of(
            new SalesFact("R-1", OffsetDateTime.parse("2026-03-15T10:00:00+02:00"), "SKU-1", "Apple", 1, new BigDecimal("3.50")),
            new SalesFact("R-2", OffsetDateTime.parse("2026-03-16T11:00:00+02:00"), "SKU-2", "Banana", 1, new BigDecimal("2.00"))
        );

        DailySalesAnalyticsResult result = service.analyze(
            facts,
            new AnalyticsRuleSet(new BigDecimal("5.00"), new BigDecimal("4.00"))
        );

        assertEquals(2, result.aggregates().size());
        assertEquals(4, result.alertCandidates().size());
        assertEquals(AlertType.LOW_DAILY_REVENUE, result.alertCandidates().getFirst().type());
    }
}
