package com.library.notification;

import com.library.enums.NotificationType;

public final class MembershipNotifier {
    private final NotificationPublisher publisher;

    public MembershipNotifier(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    public void notify(String recipientId, String subject, String message) {
        publisher.publish(new NotificationEvent(
            recipientId, NotificationType.MEMBERSHIP_EXPIRED, subject, message, java.time.LocalDateTime.now()
        ));
    }
}
