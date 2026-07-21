package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.AuditLogMapper;
import com.library.model.AuditLog;

import java.util.List;

/**
 * JSON-backed repository for {@link AuditLog} entries.
 */
public final class AuditRepository extends JsonRepository<AuditLog, String> {

    public AuditRepository() {
        super(Constants.AUDIT_LOG_FILE, new AuditLogMapper(), AuditLog::id);
    }

    public List<AuditLog> findByActor(String actorId) {
        return findAll(a -> actorId != null && actorId.equals(a.actorId()));
    }

    public List<AuditLog> findByAction(String action) {
        return findAll(a -> action != null && action.equals(a.action()));
    }
}
