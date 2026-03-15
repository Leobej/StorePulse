package org.projects.repository;

import org.projects.persistence.entity.UserStoreAccess;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserStoreAccessRepository extends JpaRepository<UserStoreAccess, UUID> {
    @EntityGraph(attributePaths = "store")
    List<UserStoreAccess> findAllByUserIdOrderByStoreNameAsc(UUID userId);

    boolean existsByUserIdAndStoreId(UUID userId, UUID storeId);
}
