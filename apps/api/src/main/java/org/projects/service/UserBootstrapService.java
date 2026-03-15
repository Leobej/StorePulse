package org.projects.service;

import org.projects.persistence.entity.AppUser;
import org.projects.persistence.entity.Store;
import org.projects.persistence.entity.UserStoreAccess;
import org.projects.repository.AppUserRepository;
import org.projects.repository.StoreRepository;
import org.projects.repository.UserStoreAccessRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class UserBootstrapService implements ApplicationRunner {
    private final AppUserRepository appUserRepository;
    private final StoreRepository storeRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapUsername;
    private final String bootstrapPassword;

    public UserBootstrapService(
        AppUserRepository appUserRepository,
        StoreRepository storeRepository,
        UserStoreAccessRepository userStoreAccessRepository,
        PasswordEncoder passwordEncoder,
        @Value("${storepulse.auth.bootstrap-username}") String bootstrapUsername,
        @Value("${storepulse.auth.bootstrap-password}") String bootstrapPassword
    ) {
        this.appUserRepository = appUserRepository;
        this.storeRepository = storeRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapUsername = bootstrapUsername;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppUser existingUser = appUserRepository.findByUsername(bootstrapUsername).orElse(null);
        if (existingUser != null) {
            ensureStoreAccess(existingUser, existingUser.getStore());
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Store store = storeRepository.findByCode("default-store")
            .orElseThrow(() -> new IllegalStateException("Default store was not created"));
        AppUser user = new AppUser(
            UUID.randomUUID(),
            bootstrapUsername,
            passwordEncoder.encode(bootstrapPassword),
            "ADMIN",
            true,
            store,
            now,
            now
        );
        AppUser savedUser = appUserRepository.save(user);
        ensureStoreAccess(savedUser, store);
    }

    private void ensureStoreAccess(AppUser user, Store store) {
        if (userStoreAccessRepository.existsByUserIdAndStoreId(user.getId(), store.getId())) {
            return;
        }

        userStoreAccessRepository.save(new UserStoreAccess(
            UUID.randomUUID(),
            user,
            store,
            OffsetDateTime.now()
        ));
    }
}
