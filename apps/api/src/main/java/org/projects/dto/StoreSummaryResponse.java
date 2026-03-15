package org.projects.dto;

import java.util.UUID;

public record StoreSummaryResponse(
    UUID id,
    String code,
    String name
) {
}
