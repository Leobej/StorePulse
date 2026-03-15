package org.projects.events;

import org.projects.domain.AlertSeverity;
import org.projects.domain.AlertType;
import org.projects.eventbus.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AlertCreatedEvent(
    UUID alertId,
    AlertType type,
    AlertSeverity severity,
    LocalDate businessDate,
    Instant occurredAt
) implements DomainEvent {
}
