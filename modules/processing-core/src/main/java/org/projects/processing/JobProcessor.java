package org.projects.processing;

import java.util.Optional;
import java.util.UUID;

public interface JobProcessor {
    UUID submit(Job job);

    Optional<JobSnapshot> getJob(UUID jobId);
}
