package org.projects.dto;

import org.projects.domain.AlertSeverity;
import org.projects.domain.AlertStatus;
import org.projects.domain.AlertType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertResponse(
    UUID id,
    UUID storeId,
    AlertType type,
    AlertSeverity severity,
    AlertStatus status,
    LocalDate businessDate,
    String message,
    OffsetDateTime createdAt,
    OffsetDateTime acknowledgedAt
) {
}
