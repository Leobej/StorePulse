package org.projects.analytics;

import org.projects.domain.AlertSeverity;
import org.projects.domain.AlertType;

import java.time.LocalDate;

public record AlertCandidate(
    AlertType type,
    AlertSeverity severity,
    String message,
    LocalDate businessDate
) {
}
