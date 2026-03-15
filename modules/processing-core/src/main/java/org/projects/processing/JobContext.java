package org.projects.processing;

public interface JobContext {
    void reportProgress(int percent, String message);
}
