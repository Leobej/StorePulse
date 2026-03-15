package org.projects.scheduler;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleNodeRecurringTaskScheduler implements RecurringTaskScheduler, AutoCloseable {
    private final ScheduledExecutorService executorService;

    public SingleNodeRecurringTaskScheduler(int threadCount) {
        this.executorService = Executors.newScheduledThreadPool(threadCount, new NamedThreadFactory());
    }

    @Override
    public ScheduledTask scheduleWithFixedDelay(String taskName, Runnable task, Duration initialDelay, Duration delay) {
        var future = executorService.scheduleWithFixedDelay(
            task,
            initialDelay.toMillis(),
            delay.toMillis(),
            TimeUnit.MILLISECONDS
        );
        return () -> future.cancel(false);
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("storepulse-scheduler-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
