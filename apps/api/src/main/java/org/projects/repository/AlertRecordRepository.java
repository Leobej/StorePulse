package org.projects.repository;

import org.projects.domain.AlertStatus;
import org.projects.domain.AlertType;
import org.projects.persistence.entity.AlertRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRecordRepository extends JpaRepository<AlertRecord, UUID> {
    List<AlertRecord> findAllByOrderByCreatedAtDesc();

    List<AlertRecord> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Optional<AlertRecord> findByIdAndStoreId(UUID id, UUID storeId);

    Optional<AlertRecord> findByStoreIdAndTypeAndBusinessDateAndStatus(UUID storeId, AlertType type, LocalDate businessDate, AlertStatus status);
}
