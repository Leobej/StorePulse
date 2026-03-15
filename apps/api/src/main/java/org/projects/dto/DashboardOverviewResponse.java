package org.projects.dto;

import java.util.List;

public record DashboardOverviewResponse(
    List<DailyAggregateResponse> dailyAggregates,
    List<AlertResponse> openAlerts
) {
}
