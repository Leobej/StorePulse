package org.projects.processing;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryJobProcessorTest {
    @Test
    void runsSubmittedJobToCompletion() throws Exception {
        try (InMemoryJobProcessor processor = new InMemoryJobProcessor(1)) {
            UUID jobId = processor.submit(context -> context.reportProgress(50, "Parsing"));

            JobSnapshot snapshot = waitForCompletion(processor, jobId);

            assertEquals(JobState.COMPLETED, snapshot.state());
            assertEquals(100, snapshot.progressPercent());
            assertEquals("Completed", snapshot.progressMessage());
            assertNotNull(snapshot.startedAt());
            assertNotNull(snapshot.finishedAt());
        }
    }

    private static JobSnapshot waitForCompletion(InMemoryJobProcessor processor, UUID jobId) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            JobSnapshot snapshot = processor.getJob(jobId).orElseThrow();
            if (snapshot.state() == JobState.COMPLETED || snapshot.state() == JobState.FAILED) {
                return snapshot;
            }
            Thread.sleep(20L);
        }

        assertTrue(false, "Job did not complete in time");
        return processor.getJob(jobId).orElseThrow();
    }
}
