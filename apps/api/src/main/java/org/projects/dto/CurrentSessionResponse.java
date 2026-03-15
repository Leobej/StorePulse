package org.projects.dto;

import java.util.List;

public record CurrentSessionResponse(
    String username,
    StoreSummaryResponse currentStore,
    List<StoreSummaryResponse> allowedStores
) {
}
