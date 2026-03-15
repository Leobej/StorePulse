package org.projects.repository;

import org.projects.persistence.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    @EntityGraph(attributePaths = "store")
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
