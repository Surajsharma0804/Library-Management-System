package com.library.notification;

import com.library.enums.NotificationType;

public final class DueDateNotifier {
    private final NotificationPublisher publisher;

    public DueDateNotifier(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    public void notify(String recipientId, String subject, String message) {
        publisher.publish(new NotificationEvent(
            recipientId, NotificationType.DUE_DATE_REMINDER, subject, message, java.time.LocalDateTime.now()
        ));
    }
}
