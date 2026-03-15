package org.projects.repository;

import org.projects.persistence.entity.ImportRowError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImportRowErrorRepository extends JpaRepository<ImportRowError, UUID> {
    List<ImportRowError> findByImportBatchIdOrderByLineNumberAsc(UUID importBatchId);

    void deleteByImportBatchId(UUID importBatchId);
}
