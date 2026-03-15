package org.projects.service;

import org.projects.domain.ImportStatus;
import org.projects.events.ImportCompletedEvent;
import org.projects.eventbus.EventBus;
import org.projects.ingest.IngestResult;
import org.projects.ingest.RowError;
import org.projects.ingest.SalesCsvIngestor;
import org.projects.ingest.SalesRecord;
import org.projects.persistence.entity.ImportBatch;
import org.projects.persistence.entity.ImportRowError;
import org.projects.persistence.entity.SalesRecordEntity;
import org.projects.repository.ImportBatchRepository;
import org.projects.repository.ImportRowErrorRepository;
import org.projects.repository.SalesRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ImportExecutionService {
    private final ImportBatchRepository batchRepository;
    private final SalesRecordRepository salesRecordRepository;
    private final ImportRowErrorRepository importRowErrorRepository;
    private final SalesCsvIngestor salesCsvIngestor;
    private final EventBus eventBus;

    public ImportExecutionService(
        ImportBatchRepository batchRepository,
        SalesRecordRepository salesRecordRepository,
        ImportRowErrorRepository importRowErrorRepository,
        SalesCsvIngestor salesCsvIngestor,
        EventBus eventBus
    ) {
        this.batchRepository = batchRepository;
        this.salesRecordRepository = salesRecordRepository;
        this.importRowErrorRepository = importRowErrorRepository;
        this.salesCsvIngestor = salesCsvIngestor;
        this.eventBus = eventBus;
    }

    public void processSalesImport(UUID batchId) throws IOException {
        markProcessing(batchId);

        ImportBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Import batch not found: " + batchId));

        try (InputStream inputStream = Files.newInputStream(Path.of(batch.getFilePath()))) {
            IngestResult result = salesCsvIngestor.ingest(inputStream);
            markCompleted(batchId, result);
            eventBus.publish(new ImportCompletedEvent(batchId, java.time.Instant.now()));
        } catch (Exception ex) {
            markFailed(batchId, ex.getMessage() == null ? "Import failed" : ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public void markProcessing(UUID batchId) {
        ImportBatch batch = getBatch(batchId);
        batch.setImportStatus(ImportStatus.PROCESSING);
        batch.setUpdatedAt(OffsetDateTime.now());
        batch.setErrorMessage(null);
        batchRepository.save(batch);
    }

    @Transactional
    public void markCompleted(UUID batchId, IngestResult result) {
        ImportBatch batch = getBatch(batchId);
        clearExistingBatchArtifacts(batchId);
        persistSalesRecords(batch, result.records());
        persistRowErrors(batch, result.rowErrors());
        batch.setImportStatus(ImportStatus.COMPLETED);
        batch.setProcessedRows(result.rowsRead());
        batch.setSuccessfulRows(result.validRows());
        batch.setFailedRows(result.invalidRows());
        batch.setUpdatedAt(OffsetDateTime.now());
        batch.setErrorMessage(result.invalidRows() > 0 ? "Completed with " + result.invalidRows() + " invalid row(s)" : null);
        batchRepository.save(batch);
    }

    @Transactional
    public void markFailed(UUID batchId, String errorMessage) {
        ImportBatch batch = getBatch(batchId);
        batch.setImportStatus(ImportStatus.FAILED);
        batch.setUpdatedAt(OffsetDateTime.now());
        batch.setErrorMessage(errorMessage);
        batchRepository.save(batch);
    }

    private ImportBatch getBatch(UUID batchId) {
        return batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Import batch not found: " + batchId));
    }

    private void clearExistingBatchArtifacts(UUID batchId) {
        salesRecordRepository.deleteByImportBatchId(batchId);
        importRowErrorRepository.deleteByImportBatchId(batchId);
    }

    private void persistSalesRecords(ImportBatch batch, List<SalesRecord> records) {
        OffsetDateTime now = OffsetDateTime.now();
        List<SalesRecordEntity> entities = records.stream()
            .map(record -> new SalesRecordEntity(
                UUID.randomUUID(),
                batch,
                batch.getStore(),
                record.receiptId(),
                record.soldAt(),
                record.productSku(),
                record.productName(),
                record.quantity(),
                record.unitPrice(),
                record.totalAmount(),
                now
            ))
            .toList();
        salesRecordRepository.saveAll(entities);
    }

    private void persistRowErrors(ImportBatch batch, List<RowError> rowErrors) {
        OffsetDateTime now = OffsetDateTime.now();
        List<ImportRowError> entities = rowErrors.stream()
            .map(rowError -> new ImportRowError(
                UUID.randomUUID(),
                batch,
                rowError.lineNumber(),
                rowError.message(),
                now
            ))
            .toList();
        importRowErrorRepository.saveAll(entities);
    }
}
