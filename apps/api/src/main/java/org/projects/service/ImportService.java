package org.projects.service;

import org.projects.domain.ImportStatus;
import org.projects.domain.ImportType;
import org.projects.dto.ImportBatchResponse;
import org.projects.persistence.entity.ImportBatch;
import org.projects.repository.ImportBatchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ImportService {
    private final ImportBatchRepository batchRepository;

    private final Path storageDir;

    public ImportService(ImportBatchRepository batchRepository, @Value("${storepulse.import.storage-dir}") Path storageDir) {
        this.batchRepository = batchRepository;
        this.storageDir = storageDir;
    }

    public ImportBatchResponse createSalesImport(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        UUID batchId = UUID.randomUUID();
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.csv";

        Files.createDirectories(storageDir);

        Path targetPath = storageDir.resolve(batchId.toString() + ".csv");

        Files.copy(file.getInputStream(), targetPath);
        OffsetDateTime now = OffsetDateTime.now();

        ImportBatch batch = new ImportBatch(batchId, ImportType.SALES, ImportStatus.UPLOADED, targetPath.toAbsolutePath().toString(), originalFileName, now);

        ImportBatch saved = batchRepository.save(batch);

        return new ImportBatchResponse(saved.getId(), saved.getImportType(), saved.getImportStatus(), saved.getOriginalFileName(), saved.getCreatedAt());
    }
}
