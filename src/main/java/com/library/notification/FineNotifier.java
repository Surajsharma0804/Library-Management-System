package com.library.notification;

import com.library.enums.NotificationType;

public final class FineNotifier {
    private final NotificationPublisher publisher;

    public FineNotifier(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    public void notify(String recipientId, String subject, String message) {
        publisher.publish(new NotificationEvent(
            recipientId, NotificationType.FINE_RECORDED, subject, message, java.time.LocalDateTime.now()
        ));
    }
}
