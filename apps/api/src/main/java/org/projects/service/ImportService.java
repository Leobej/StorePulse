package org.projects.service;

import org.projects.domain.ImportStatus;
import org.projects.domain.ImportType;
import org.projects.dto.ImportBatchResponse;
import org.projects.persistence.entity.ImportBatch;
import org.projects.repository.ImportBatchRepository;
import org.projects.processing.JobProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ImportService {
    private final ImportBatchRepository batchRepository;
    private final ImportExecutionService importExecutionService;
    private final JobProcessor jobProcessor;
    private final ChecksumService checksumService;
    private final CurrentStoreContextService currentStoreContextService;
    private final Path storageDir;

    public ImportService(
        ImportBatchRepository batchRepository,
        ImportExecutionService importExecutionService,
        JobProcessor jobProcessor,
        ChecksumService checksumService,
        CurrentStoreContextService currentStoreContextService,
        @Value("${storepulse.import.storage-dir}") Path storageDir
    ) {
        this.batchRepository = batchRepository;
        this.importExecutionService = importExecutionService;
        this.jobProcessor = jobProcessor;
        this.checksumService = checksumService;
        this.currentStoreContextService = currentStoreContextService;
        this.storageDir = storageDir;
    }

    public ImportBatchResponse createSalesImport(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Empty file");
        }

        UUID batchId = UUID.randomUUID();
        var store = currentStoreContextService.getCurrentStore();
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.csv";
        String fileChecksum;

        try (InputStream inputStream = file.getInputStream()) {
            fileChecksum = checksumService.sha256(inputStream);
        }

        batchRepository.findByStoreIdAndImportTypeAndFileChecksum(store.getId(), ImportType.SALES, fileChecksum)
            .ifPresent(existing -> {
                throw new ResponseStatusException(CONFLICT, "Duplicate sales import already exists: " + existing.getId());
            });

        Files.createDirectories(storageDir);

        Path targetPath = storageDir.resolve(batchId.toString() + ".csv");

        Files.copy(file.getInputStream(), targetPath);
        OffsetDateTime now = OffsetDateTime.now();

        ImportBatch batch = new ImportBatch(
            batchId,
            store,
            ImportType.SALES,
            ImportStatus.UPLOADED,
            targetPath.toAbsolutePath().toString(),
            originalFileName,
            fileChecksum,
            now
        );

        ImportBatch saved = batchRepository.save(batch);

        jobProcessor.submit(context -> {
            context.reportProgress(10, "Starting import");
            importExecutionService.processSalesImport(saved.getId());
            context.reportProgress(100, "Import completed");
        });

        return toResponse(saved);
    }

    public ImportBatchResponse getImportBatch(UUID batchId) {
        ImportBatch batch = batchRepository.findByIdAndStoreId(batchId, currentStoreContextService.getCurrentStoreId())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Import batch not found"));
        return toResponse(batch);
    }

    public java.util.List<ImportBatchResponse> listImports() {
        return batchRepository.findAllByStoreIdOrderByCreatedAtDesc(currentStoreContextService.getCurrentStoreId()).stream()
            .map(this::toResponse)
            .toList();
    }

    private ImportBatchResponse toResponse(ImportBatch batch) {
        return new ImportBatchResponse(
            batch.getId(),
            batch.getStore().getId(),
            batch.getImportType(),
            batch.getImportStatus(),
            batch.getOriginalFileName(),
            batch.getFileChecksum(),
            batch.getCreatedAt(),
            batch.getUpdatedAt(),
            batch.getErrorMessage(),
            batch.getProcessedRows(),
            batch.getSuccessfulRows(),
            batch.getFailedRows()
        );
    }
}
