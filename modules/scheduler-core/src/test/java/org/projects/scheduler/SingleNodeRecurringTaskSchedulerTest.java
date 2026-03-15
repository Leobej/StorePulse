package org.projects.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleNodeRecurringTaskSchedulerTest {
    @Test
    void runsRecurringTask() throws Exception {
        try (SingleNodeRecurringTaskScheduler scheduler = new SingleNodeRecurringTaskScheduler(1)) {
            AtomicInteger runs = new AtomicInteger();
            ScheduledTask task = scheduler.scheduleWithFixedDelay(
                "test",
                runs::incrementAndGet,
                Duration.ZERO,
                Duration.ofMillis(20)
            );

            Thread.sleep(80L);
            task.cancel();

            assertTrue(runs.get() >= 2);
        }
    }
}
