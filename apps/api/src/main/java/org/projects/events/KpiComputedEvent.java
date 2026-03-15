package org.projects.events;

import org.projects.eventbus.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;

public record KpiComputedEvent(
    LocalDate businessDate,
    Instant occurredAt
) implements DomainEvent {
}
