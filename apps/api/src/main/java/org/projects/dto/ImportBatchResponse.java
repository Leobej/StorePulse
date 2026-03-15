package org.projects.dto;

import org.projects.domain.ImportType;
import org.projects.domain.ImportStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ImportBatchResponse {

    private UUID id;
    private UUID storeId;
    private ImportType type;
    private ImportStatus status;
    private String originalFileName;
    private String fileChecksum;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String errorMessage;
    private int processedRows;
    private int successfulRows;
    private int failedRows;

    public ImportBatchResponse(
        UUID id,
        UUID storeId,
        ImportType type,
        ImportStatus status,
        String originalFileName,
        String fileChecksum,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String errorMessage,
        int processedRows,
        int successfulRows,
        int failedRows
    ) {
        this.id = id;
        this.storeId = storeId;
        this.type = type;
        this.status = status;
        this.originalFileName = originalFileName;
        this.fileChecksum = fileChecksum;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.errorMessage = errorMessage;
        this.processedRows = processedRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
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

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
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

    public int getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(int processedRows) {
        this.processedRows = processedRows;
    }

    public int getSuccessfulRows() {
        return successfulRows;
    }

    public void setSuccessfulRows(int successfulRows) {
        this.successfulRows = successfulRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(int failedRows) {
        this.failedRows = failedRows;
    }
}
