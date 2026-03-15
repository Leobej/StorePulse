package org.projects.persistence.entity;

import jakarta.persistence.*;
import org.projects.domain.ImportStatus;
import org.projects.domain.ImportType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table
public class ImportBatch {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private ImportType importType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ImportStatus importStatus;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    public ImportBatch(UUID id, ImportType importType, ImportStatus importStatus, String filePath, String originalFileName, OffsetDateTime createdAt) {
        this.id = id;
        this.importType = importType;
        this.importStatus = importStatus;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        ;
    }

    public ImportBatch() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ImportType getImportType() {
        return importType;
    }

    public void setImportType(ImportType importType) {
        this.importType = importType;
    }

    public ImportStatus getImportStatus() {
        return importStatus;
    }

    public void setImportStatus(ImportStatus importStatus) {
        this.importStatus = importStatus;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
