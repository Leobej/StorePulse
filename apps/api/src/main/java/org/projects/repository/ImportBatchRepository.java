package org.projects.repository;

import org.projects.persistence.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, UUID> {
}
