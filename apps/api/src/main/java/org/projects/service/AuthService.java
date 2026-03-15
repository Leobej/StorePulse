package org.projects.service;

import org.projects.dto.AuthRequest;
import org.projects.dto.AuthResponse;
import org.projects.dto.CurrentSessionResponse;
import org.projects.dto.StoreSummaryResponse;
import org.projects.persistence.entity.AppUser;
import org.projects.persistence.entity.Store;
import org.projects.repository.AppUserRepository;
import org.projects.repository.UserStoreAccessRepository;
import org.projects.security.JwtService;
import org.projects.security.StorePulseUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final StorePulseUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final CurrentStoreContextService currentStoreContextService;
    private final UserStoreAccessRepository userStoreAccessRepository;

    public AuthService(
        AuthenticationManager authenticationManager,
        StorePulseUserDetailsService userDetailsService,
        JwtService jwtService,
        AppUserRepository appUserRepository,
        CurrentStoreContextService currentStoreContextService,
        UserStoreAccessRepository userStoreAccessRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
        this.currentStoreContextService = currentStoreContextService;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    public AuthResponse login(AuthRequest authRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());
        AppUser user = appUserRepository.findByUsername(authRequest.username()).orElseThrow();
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(
            token,
            "Bearer",
            jwtService.expirationSeconds(),
            userDetails.getUsername(),
            toStoreSummary(user.getStore()),
            userStoreAccessRepository.findAllByUserIdOrderByStoreNameAsc(user.getId()).stream()
                .map(access -> toStoreSummary(access.getStore()))
                .toList()
        );
    }

    public CurrentSessionResponse currentSession() {
        AppUser user = currentStoreContextService.getCurrentUser();
        return toCurrentSession(user);
    }

    @Transactional
    public CurrentSessionResponse switchStore(UUID storeId) {
        AppUser user = currentStoreContextService.getCurrentUser();
        if (!userStoreAccessRepository.existsByUserIdAndStoreId(user.getId(), storeId)) {
            throw new ResponseStatusException(FORBIDDEN, "Store access denied");
        }

        Store store = userStoreAccessRepository.findAllByUserIdOrderByStoreNameAsc(user.getId()).stream()
            .map(access -> access.getStore())
            .filter(candidate -> candidate.getId().equals(storeId))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));

        user.setStore(store);
        AppUser savedUser = appUserRepository.save(user);
        return toCurrentSession(savedUser);
    }

    private CurrentSessionResponse toCurrentSession(AppUser user) {
        List<StoreSummaryResponse> allowedStores = userStoreAccessRepository.findAllByUserIdOrderByStoreNameAsc(user.getId()).stream()
            .map(access -> toStoreSummary(access.getStore()))
            .toList();
        return new CurrentSessionResponse(
            user.getUsername(),
            toStoreSummary(user.getStore()),
            allowedStores
        );
    }

    private StoreSummaryResponse toStoreSummary(Store store) {
        return new StoreSummaryResponse(store.getId(), store.getCode(), store.getName());
    }
}
