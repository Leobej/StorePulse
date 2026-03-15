package org.projects.processing;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryJobProcessor implements JobProcessor, AutoCloseable {
    private final ExecutorService executorService;
    private final ConcurrentMap<UUID, MutableJobSnapshot> snapshots = new ConcurrentHashMap<>();

    public InMemoryJobProcessor(int workerCount) {
        this.executorService = Executors.newFixedThreadPool(workerCount, new NamedThreadFactory());
    }

    @Override
    public UUID submit(Job job) {
        UUID jobId = UUID.randomUUID();
        MutableJobSnapshot snapshot = MutableJobSnapshot.queued(jobId);
        snapshots.put(jobId, snapshot);

        executorService.submit(() -> execute(jobId, job, snapshot));
        return jobId;
    }

    @Override
    public Optional<JobSnapshot> getJob(UUID jobId) {
        MutableJobSnapshot snapshot = snapshots.get(jobId);
        return snapshot == null ? Optional.empty() : Optional.of(snapshot.toSnapshot());
    }

    private void execute(UUID jobId, Job job, MutableJobSnapshot snapshot) {
        snapshot.markRunning();
        try {
            job.execute((percent, message) -> snapshot.updateProgress(percent, message));
            snapshot.markCompleted();
        } catch (Exception ex) {
            snapshot.markFailed(ex.getMessage() == null ? "Job failed" : ex.getMessage());
        }
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private static final class MutableJobSnapshot {
        private final UUID jobId;
        private final Instant queuedAt;
        private volatile JobState state;
        private volatile int progressPercent;
        private volatile String progressMessage;
        private volatile Instant startedAt;
        private volatile Instant finishedAt;
        private volatile String errorMessage;

        private MutableJobSnapshot(UUID jobId, Instant queuedAt) {
            this.jobId = jobId;
            this.queuedAt = queuedAt;
            this.state = JobState.QUEUED;
            this.progressPercent = 0;
            this.progressMessage = "Queued";
        }

        private static MutableJobSnapshot queued(UUID jobId) {
            return new MutableJobSnapshot(jobId, Instant.now());
        }

        private void markRunning() {
            this.state = JobState.RUNNING;
            this.startedAt = Instant.now();
            this.progressMessage = "Running";
        }

        private void updateProgress(int progressPercent, String progressMessage) {
            this.progressPercent = Math.max(0, Math.min(100, progressPercent));
            this.progressMessage = progressMessage;
        }

        private void markCompleted() {
            this.state = JobState.COMPLETED;
            this.progressPercent = 100;
            this.progressMessage = "Completed";
            this.finishedAt = Instant.now();
        }

        private void markFailed(String errorMessage) {
            this.state = JobState.FAILED;
            this.errorMessage = errorMessage;
            this.progressMessage = "Failed";
            this.finishedAt = Instant.now();
        }

        private JobSnapshot toSnapshot() {
            return new JobSnapshot(
                jobId,
                state,
                progressPercent,
                progressMessage,
                queuedAt,
                startedAt,
                finishedAt,
                errorMessage
            );
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("storepulse-processing-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
