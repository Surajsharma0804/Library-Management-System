package com.library.service;

import com.library.notification.NotificationEvent;
import com.library.notification.NotificationPublisher;
import com.library.util.DateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class NotificationService implements Consumer<NotificationEvent> {

    public record Notification(
            long id,
            String recipientRegistrationNumber,
            String category,
            String subject,
            String message,
            LocalDateTime timestamp,
            boolean read
    ) {
        public Notification withRead(boolean read) {
            return new Notification(id, recipientRegistrationNumber, category, subject, message, timestamp, read);
        }
    }

    private final Map<String, List<Notification>> inbox = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public NotificationService(NotificationPublisher publisher) {
        publisher.subscribe(this);
    }

    @Override
    public void accept(NotificationEvent event) {
        long id = idSeq.getAndIncrement();
        Notification n = new Notification(
                id,
                event.recipientRegistrationNumber(),
                event.type() != null ? event.type().name() : "GENERAL",
                event.subject(),
                event.message(),
                event.timestamp(),
                false);
        inbox.computeIfAbsent(event.recipientRegistrationNumber(),
                        k -> Collections.synchronizedList(new ArrayList<>()))
                .add(n);
    }

    public void handleEvent(NotificationEvent event) {
        accept(event);
    }

    public void send(String recipient, String category, String subject, String message) {
        accept(new NotificationEvent(recipient,
                category != null ? com.library.enums.NotificationType.valueOf(category) : com.library.enums.NotificationType.GENERAL,
                subject, message, DateUtils.now()));
    }

    public List<Notification> inboxFor(String registrationNumber) {
        Objects.requireNonNull(registrationNumber);
        return List.copyOf(inbox.getOrDefault(registrationNumber, List.of()));
    }

    public List<Notification> unreadFor(String registrationNumber) {
        return inboxFor(registrationNumber).stream().filter(n -> !n.read()).toList();
    }

    public void markAllRead(String registrationNumber) {
        List<Notification> list = inbox.get(registrationNumber);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.set(i, list.get(i).withRead(true));
            }
        }
    }

    public long unreadCount(String registrationNumber) {
        return inboxFor(registrationNumber).stream().filter(n -> !n.read()).count();
    }
}
