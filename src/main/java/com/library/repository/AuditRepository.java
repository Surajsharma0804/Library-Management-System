package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.AuditLogMapper;
import com.library.model.AuditLog;

import java.util.List;

/**
 * JSON-backed repository for {@link AuditLog} entries.
 * Uses secondary indexes for O(1) lookups by actor and actionType.
 */
public final class AuditRepository extends IndexedRepository<AuditLog, String> {

    public AuditRepository() {
        super(Constants.AUDIT_LOG_FILE, new AuditLogMapper(), AuditLog::id);
        registerSecondaryIndex("actor");
        registerSecondaryIndex("actionType");
    }

    @Override
    protected String secondaryKey(String indexName, AuditLog entity) {
        return switch (indexName) {
            case "actor"      -> entity.actorId();
            case "actionType" -> entity.action();
            default           -> null;
        };
    }

    public List<AuditLog> findByActor(String actorId) {
        return findAllBySecondaryKey("actor", actorId);
    }

    public List<AuditLog> findByAction(String action) {
        return findAllBySecondaryKey("actionType", action);
    }
}
