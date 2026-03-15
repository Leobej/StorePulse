package org.projects.eventbus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class InMemoryEventBus implements EventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    @Override
    public <T extends DomainEvent> void publish(T event) {
        List<Consumer<?>> typedListeners = listeners.getOrDefault(event.getClass(), List.of());
        for (Consumer<?> listener : typedListeners) {
            @SuppressWarnings("unchecked")
            Consumer<T> typed = (Consumer<T>) listener;
            typed.accept(event);
        }
    }

    @Override
    public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(listener);
    }
}
