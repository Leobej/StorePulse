package org.projects.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    String username,
    StoreSummaryResponse currentStore,
    java.util.List<StoreSummaryResponse> allowedStores
) {
}
