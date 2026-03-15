package org.projects.eventbus;

import java.util.function.Consumer;

public interface EventBus {
    <T extends DomainEvent> void publish(T event);

    <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> listener);
}
