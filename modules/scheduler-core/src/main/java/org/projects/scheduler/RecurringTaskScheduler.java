package org.projects.scheduler;

import java.time.Duration;

public interface RecurringTaskScheduler {
    ScheduledTask scheduleWithFixedDelay(String taskName, Runnable task, Duration initialDelay, Duration delay);
}
