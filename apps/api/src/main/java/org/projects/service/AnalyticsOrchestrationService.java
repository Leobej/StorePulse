package org.projects.service;

import org.projects.events.ImportCompletedEvent;
import org.projects.eventbus.EventBus;
import org.projects.scheduler.RecurringTaskScheduler;
import org.projects.scheduler.ScheduledTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AnalyticsOrchestrationService {
    private final EventBus eventBus;
    private final AnalyticsMaterializationService analyticsMaterializationService;
    private final RecurringTaskScheduler recurringTaskScheduler;
    private final Duration recomputeDelay;
    private ScheduledTask scheduledTask;

    public AnalyticsOrchestrationService(
        EventBus eventBus,
        AnalyticsMaterializationService analyticsMaterializationService,
        RecurringTaskScheduler recurringTaskScheduler,
        @Value("${storepulse.scheduler.analytics-recompute-delay}") Duration recomputeDelay
    ) {
        this.eventBus = eventBus;
        this.analyticsMaterializationService = analyticsMaterializationService;
        this.recurringTaskScheduler = recurringTaskScheduler;
        this.recomputeDelay = recomputeDelay;
    }

    @jakarta.annotation.PostConstruct
    void start() {
        eventBus.subscribe(ImportCompletedEvent.class, event -> analyticsMaterializationService.recomputeAllDailyAggregates());
        scheduledTask = recurringTaskScheduler.scheduleWithFixedDelay(
            "analytics-recompute",
            analyticsMaterializationService::recomputeAllDailyAggregates,
            recomputeDelay,
            recomputeDelay
        );
    }

    @jakarta.annotation.PreDestroy
    void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
    }
}
