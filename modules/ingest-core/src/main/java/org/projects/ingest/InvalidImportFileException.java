package org.projects.ingest;

public class InvalidImportFileException extends RuntimeException {
    public InvalidImportFileException(String message) {
        super(message);
    }
}
