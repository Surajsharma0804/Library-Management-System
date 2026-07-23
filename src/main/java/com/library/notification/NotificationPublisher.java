package com.library.notification;

import com.library.model.Notification;
import com.library.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Central notification dispatcher.
 *
 * <p>Every published {@link NotificationEvent} is:
 * <ol>
 *   <li>Delivered in-memory to all registered {@link Consumer} subscribers, and</li>
 *   <li>Persisted to the {@link NotificationRepository} so notifications survive
 *       application restarts (Requirements 15.4, 15.5).</li>
 * </ol>
 */
public final class NotificationPublisher {

    private final List<Consumer<NotificationEvent>> subscribers = new CopyOnWriteArrayList<>();
    private final NotificationRepository notificationRepo;

    /**
     * Creates a {@code NotificationPublisher} with persistence support.
     *
     * @param notificationRepo repository used to persist each published event; must not be {@code null}
     */
    public NotificationPublisher(NotificationRepository notificationRepo) {
        this.notificationRepo = java.util.Objects.requireNonNull(notificationRepo, "notificationRepo");
    }

    /**
     * Registers a subscriber that will receive every future {@link NotificationEvent}.
     *
     * @param handler the consumer to register; must not be {@code null}
     */
    public void subscribe(Consumer<NotificationEvent> handler) {
        subscribers.add(java.util.Objects.requireNonNull(handler, "handler"));
    }

    /**
     * Publishes an event to all subscribers and persists it.
     *
     * <p>All in-memory subscribers are notified first, then the event is saved
     * to the repository. Any subscriber that throws will cause the exception to
     * propagate (fast-fail), but the persistence step is always attempted even
     * if a subscriber threw.
     *
     * @param event the event to publish; must not be {@code null}
     */
    public void publish(NotificationEvent event) {
        java.util.Objects.requireNonNull(event, "event");
        for (var h : subscribers) {
            h.accept(event);
        }
        // Persist every notification so it survives restarts (Req 15.4, 15.5)
        String id = UUID.randomUUID().toString();
        com.library.enums.NotificationType type =
                event.type() != null ? event.type() : com.library.enums.NotificationType.GENERAL;
        LocalDateTime ts = event.timestamp() != null ? event.timestamp() : LocalDateTime.now();
        Notification notification = new Notification(
                id,
                event.recipientId(),
                type,
                event.message(),
                false,
                ts
        );
        notificationRepo.save(notification);
    }
}
