package org.projects.eventbus;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryEventBusTest {
    @Test
    void deliversPublishedEventsToSubscribers() {
        InMemoryEventBus eventBus = new InMemoryEventBus();
        AtomicInteger calls = new AtomicInteger();
        eventBus.subscribe(TestEvent.class, event -> calls.incrementAndGet());

        eventBus.publish(new TestEvent());

        assertEquals(1, calls.get());
    }

    private record TestEvent(Instant occurredAt) implements DomainEvent {
        private TestEvent() {
            this(Instant.now());
        }
    }
}
