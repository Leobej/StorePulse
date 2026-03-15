package org.projects.analytics;

import org.projects.domain.AlertSeverity;
import org.projects.domain.AlertType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DailySalesAnalyticsService {
    private final SalesSummaryCalculator salesSummaryCalculator;

    public DailySalesAnalyticsService(SalesSummaryCalculator salesSummaryCalculator) {
        this.salesSummaryCalculator = salesSummaryCalculator;
    }

    public DailySalesAnalyticsResult analyze(List<SalesFact> facts, AnalyticsRuleSet ruleSet) {
        Map<LocalDate, List<SalesFact>> factsByDay = facts.stream()
            .collect(Collectors.groupingBy(fact -> fact.soldAt().toLocalDate()));

        List<DailySalesAggregate> aggregates = factsByDay.entrySet().stream()
            .map(entry -> new DailySalesAggregate(entry.getKey(), salesSummaryCalculator.summarize(entry.getValue())))
            .sorted(Comparator.comparing(DailySalesAggregate::businessDate))
            .toList();

        List<AlertCandidate> alertCandidates = new ArrayList<>();
        for (DailySalesAggregate aggregate : aggregates) {
            SalesSummary summary = aggregate.summary();
            if (summary.revenue().compareTo(ruleSet.minDailyRevenue()) < 0) {
                alertCandidates.add(new AlertCandidate(
                    AlertType.LOW_DAILY_REVENUE,
                    AlertSeverity.WARNING,
                    "Revenue for " + aggregate.businessDate() + " is below threshold",
                    aggregate.businessDate()
                ));
            }
            if (summary.receipts() > 0 && summary.averageBasket().compareTo(ruleSet.minAverageBasket()) < 0) {
                alertCandidates.add(new AlertCandidate(
                    AlertType.LOW_AVERAGE_BASKET,
                    AlertSeverity.INFO,
                    "Average basket for " + aggregate.businessDate() + " is below threshold",
                    aggregate.businessDate()
                ));
            }
        }

        return new DailySalesAnalyticsResult(aggregates, List.copyOf(alertCandidates));
    }
}
