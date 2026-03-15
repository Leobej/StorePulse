package org.projects.repository;

import org.projects.persistence.entity.SalesDailyAggregateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesDailyAggregateRepository extends JpaRepository<SalesDailyAggregateEntity, UUID> {
    List<SalesDailyAggregateEntity> findAllByOrderByBusinessDateDesc();

    List<SalesDailyAggregateEntity> findAllByStoreIdOrderByBusinessDateDesc(UUID storeId);

    Optional<SalesDailyAggregateEntity> findByStoreIdAndBusinessDate(UUID storeId, LocalDate businessDate);
}
