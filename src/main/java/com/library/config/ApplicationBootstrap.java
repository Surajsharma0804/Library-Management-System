package com.library.config;

import com.library.enums.NotificationType;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.Administrator;
import com.library.model.Notification;
import com.library.model.User;
import com.library.notification.NotificationEvent;
import com.library.security.PasswordHasher;

import java.util.List;

/**
 * One-time bootstrap: seeds a default admin on first run and
 * pre-warms the in-memory notification inbox from persisted data
 * (Requirements 15.4, 15.5).
 */
public final class ApplicationBootstrap {

    private ApplicationBootstrap() {}

    /**
     * Initialises the application on startup.
     *
     * <ol>
     *   <li>Seeds a default administrator if none exists.</li>
     *   <li>Replays all persisted {@link Notification} records into the
     *       in-memory {@link com.library.service.NotificationService} so that
     *       notifications are available without a re-publish.</li>
     * </ol>
     *
     * @param facade the fully-wired {@link LibraryFacade}; must not be {@code null}
     */
    public static void initialise(LibraryFacade facade) {
        // 1. Seed default admin
        for (User u : facade.staffRepo().findAll()) {
            if (u.getRole() == UserRole.ADMIN) {
                break;
            }
        }
        boolean hasAdmin = facade.staffRepo().findAll().stream()
                .anyMatch(u -> u.getRole() == UserRole.ADMIN);
        if (!hasAdmin) {
            Administrator admin = Administrator.builder()
                    .id("admin-000001").username("admin").firstName("System").lastName("Administrator")
                    .passwordHash(PasswordHasher.hash("admin@123")).active(true).build();
            facade.staffRepo().save(admin);
        }

        // 2. Pre-warm in-memory notification inbox from persisted records (Req 15.4, 15.5)
        List<Notification> persisted = facade.notificationRepo().findAll();
        for (Notification n : persisted) {
            NotificationType type = n.getType() != null ? n.getType() : NotificationType.GENERAL;
            NotificationEvent event = new NotificationEvent(
                    n.getStudentId(),
                    type,
                    n.getMessage(), // subject re-uses the message (notifications have no separate subject)
                    n.getMessage(),
                    n.getCreatedAt()
            );
            facade.notifications().handleEvent(event);
        }
    }
}
