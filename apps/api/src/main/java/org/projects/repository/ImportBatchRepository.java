package org.projects.repository;

import org.projects.persistence.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, UUID> {
    java.util.List<ImportBatch> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Optional<ImportBatch> findByIdAndStoreId(UUID id, UUID storeId);

    Optional<ImportBatch> findByStoreIdAndImportTypeAndFileChecksum(UUID storeId, org.projects.domain.ImportType importType, String fileChecksum);
}
