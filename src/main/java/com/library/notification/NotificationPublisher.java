package com.library.notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NotificationPublisher {
    private final List<java.util.function.Consumer<NotificationEvent>> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(java.util.function.Consumer<NotificationEvent> handler) {
        subscribers.add(handler);
    }

    public void publish(NotificationEvent event) {
        for (var h : subscribers) h.accept(event);
    }
}
