package org.projects.dto;

import org.projects.domain.ImportStatus;
import org.projects.domain.ImportType;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ImportBatchResponse {

    private UUID id;
    private ImportType type;
    private ImportStatus status;
    private String originalFileName;
    private OffsetDateTime createdAt;

    public ImportBatchResponse(UUID id, ImportType type, ImportStatus status, String originalFileName, OffsetDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.originalFileName = originalFileName;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ImportType getType() {
        return type;
    }

    public void setType(ImportType type) {
        this.type = type;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public void setStatus(ImportStatus status) {
        this.status = status;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ImportBatchResponse() {
    }
}
