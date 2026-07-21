package com.library.notification;

import com.library.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationEvent(
        String recipientId,
        NotificationType type,
        String subject,
        String message,
        LocalDateTime timestamp
) {
    public NotificationEvent {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }

    public String recipientRegistrationNumber() { return recipientId(); }
}
