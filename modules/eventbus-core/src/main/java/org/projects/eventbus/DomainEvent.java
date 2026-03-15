package org.projects.eventbus;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
