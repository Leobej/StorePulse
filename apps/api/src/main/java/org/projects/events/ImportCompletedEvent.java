package org.projects.events;

import org.projects.eventbus.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ImportCompletedEvent(
    UUID batchId,
    Instant occurredAt
) implements DomainEvent {
}
