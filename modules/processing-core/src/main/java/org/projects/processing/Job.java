package org.projects.processing;

@FunctionalInterface
public interface Job {
    void execute(JobContext context) throws Exception;
}
