package com.library.repository;

import com.library.config.Constants;
import com.library.interfaces.JsonMappable;
import com.library.model.Notification;

import java.util.List;
import java.util.Map;

/**
 * JSON-backed repository for {@link Notification} records.
 */
public final class NotificationRepository extends JsonRepository<Notification, String> {

    private static final JsonMappable<Notification> MAPPER = new JsonMappable<>() {
        @Override
        public Map<String, Object> toMap(Notification n) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", n.getId());
            m.put("studentId", n.getStudentId());
            m.put("type", n.getType().name());
            m.put("message", n.getMessage());
            m.put("read", n.isRead());
            m.put("createdAt", n.getCreatedAt().toString());
            return m;
        }

        @Override
        public Notification fromMap(Map<String, Object> m) {
            return new Notification(
                    (String) m.get("id"),
                    (String) m.get("studentId"),
                    com.library.enums.NotificationType.fromString((String) m.get("type")),
                    (String) m.get("message"),
                    Boolean.TRUE.equals(m.get("read")),
                    java.time.LocalDateTime.parse((String) m.get("createdAt"))
            );
        }
    };

    public NotificationRepository() {
        super(Constants.NOTIFICATIONS_FILE, MAPPER, Notification::getId);
    }

    public List<Notification> findByStudent(String studentId) {
        return findAll(n -> n.getStudentId().equals(studentId));
    }

    public List<Notification> findUnreadByStudent(String studentId) {
        return findAll(n -> n.getStudentId().equals(studentId) && !n.isRead());
    }
}
