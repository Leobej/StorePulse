package org.projects.service;

import org.projects.persistence.entity.AppUser;
import org.projects.persistence.entity.Store;
import org.projects.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class CurrentStoreContextService {
    private final AppUserRepository appUserRepository;

    public CurrentStoreContextService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthenticated request");
        }

        return appUserRepository.findByUsername(authentication.getName())
            .filter(AppUser::isActive)
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authenticated user not found"));
    }

    public Store getCurrentStore() {
        return getCurrentUser().getStore();
    }

    public UUID getCurrentStoreId() {
        return getCurrentStore().getId();
    }
}
