package org.projects.processing;

import java.time.Instant;
import java.util.UUID;

public record JobSnapshot(
    UUID jobId,
    JobState state,
    int progressPercent,
    String progressMessage,
    Instant queuedAt,
    Instant startedAt,
    Instant finishedAt,
    String errorMessage
) {
}
