package org.projects.analytics;

import java.util.List;

public record DailySalesAnalyticsResult(
    List<DailySalesAggregate> aggregates,
    List<AlertCandidate> alertCandidates
) {
}
