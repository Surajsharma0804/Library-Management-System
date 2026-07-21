package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.AuditLog;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AuditLogMapper implements JsonMappable<AuditLog> {
    @Override
    public Map<String, Object> toMap(AuditLog a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.id());
        m.put("actorId", a.actorId());
        m.put("actorRole", a.actorRole());
        m.put("action", a.action());
        m.put("targetType", a.targetType());
        m.put("targetId", a.targetId());
        m.put("details", a.details());
        m.put("timestamp", a.timestamp() != null ? a.timestamp().toString() : null);
        return m;
    }

    @Override
    public AuditLog fromMap(Map<String, Object> m) {
        String id = (String) m.get("id");
        String actorId = (String) m.get("actorId");
        String actorRole = (String) m.get("actorRole");
        String action = (String) m.get("action");
        String targetType = (String) m.get("targetType");
        String targetId = (String) m.get("targetId");
        String details = (String) m.get("details");
        String tsStr = (String) m.get("timestamp");
        LocalDateTime ts = tsStr != null ? LocalDateTime.parse(tsStr) : LocalDateTime.now();
        return new AuditLog(id, actorId, actorRole, action, targetType, targetId, details, ts);
    }
}
