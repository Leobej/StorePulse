package org.projects.repository;

import org.projects.persistence.entity.SalesRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesRecordRepository extends JpaRepository<SalesRecordEntity, UUID> {
    List<SalesRecordEntity> findByImportBatchIdOrderBySoldAtAsc(UUID importBatchId);

    List<SalesRecordEntity> findByImportBatchIdAndStoreIdOrderBySoldAtAsc(UUID importBatchId, UUID storeId);

    List<SalesRecordEntity> findByStoreIdOrderBySoldAtAsc(UUID storeId);

    void deleteByImportBatchId(UUID importBatchId);
}
