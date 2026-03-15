package org.projects.analytics;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesSummaryCalculatorTest {
    private final SalesSummaryCalculator calculator = new SalesSummaryCalculator();

    @Test
    void aggregatesRevenueUnitsReceiptsAndTopProducts() {
        List<SalesFact> facts = List.of(
            new SalesFact("R-1", OffsetDateTime.parse("2026-03-15T10:00:00+02:00"), "SKU-1", "Apple", 2, new BigDecimal("7.00")),
            new SalesFact("R-1", OffsetDateTime.parse("2026-03-15T10:05:00+02:00"), "SKU-2", "Banana", 1, new BigDecimal("1.20")),
            new SalesFact("R-2", OffsetDateTime.parse("2026-03-15T11:00:00+02:00"), "SKU-1", "Apple", 1, new BigDecimal("3.50"))
        );

        SalesSummary summary = calculator.summarize(facts);

        assertEquals(new BigDecimal("11.70"), summary.revenue());
        assertEquals(4, summary.unitsSold());
        assertEquals(2, summary.receipts());
        assertEquals(new BigDecimal("5.85"), summary.averageBasket());
        assertEquals("Apple", summary.topProducts().getFirst().productName());
        assertEquals(3, summary.topProducts().getFirst().unitsSold());
        assertEquals(new BigDecimal("8.20"), summary.hourlyDistribution().get(10).revenue());
        assertEquals(new BigDecimal("3.50"), summary.hourlyDistribution().get(11).revenue());
    }
}
