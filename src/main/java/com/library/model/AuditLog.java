package com.library.model;

import com.library.util.DateUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An immutable audit-log entry recording a single security-relevant
 * action (login, book CRUD, borrow, fine, backup, config change, etc.).
 */
public record AuditLog(
        String id,
        String actorId,
        String actorRole,
        String action,
        String targetType,
        String targetId,
        String details,
        LocalDateTime timestamp
) {
    public AuditLog {
        Objects.requireNonNull(id, "audit id");
        Objects.requireNonNull(action, "audit action");
        timestamp = timestamp == null ? DateUtils.now() : timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String actorId;
        private String actorRole;
        private String action;
        private String targetType;
        private String targetId;
        private String details;
        private LocalDateTime timestamp;

        public Builder id(String v) { this.id = v; return this; }
        public Builder actorId(String v) { this.actorId = v; return this; }
        public Builder actorRole(String v) { this.actorRole = v; return this; }
        public Builder action(String v) { this.action = v; return this; }
        public Builder targetType(String v) { this.targetType = v; return this; }
        public Builder targetId(String v) { this.targetId = v; return this; }
        public Builder details(String v) { this.details = v; return this; }
        public Builder timestamp(LocalDateTime v) { this.timestamp = v; return this; }

        public AuditLog build() {
            return new AuditLog(id, actorId, actorRole, action, targetType, targetId, details, timestamp);
        }
    }
}
