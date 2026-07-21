package com.library.service;

import com.library.factory.EntityFactory;
import com.library.model.AuditLog;
import com.library.repository.AuditRepository;
import com.library.security.Session;
import com.library.util.AppLogger;

import java.util.List;

/**
 * Application-level audit service. Every security-relevant action in
 * the service layer records an entry here so the administrator can
 * review who did what and when.
 */
public final class AuditService {

    private static final String LOG = "AuditService";
    private final AuditRepository repo;
    private final EntityFactory factory;

    public AuditService(AuditRepository repo, EntityFactory factory) {
        this.repo = repo;
        this.factory = factory;
    }

    /** Records an audit entry for the current session's user. */
    public void record(Session session, String action, String targetType, String targetId, String details) {
        AuditLog entry = factory.createAuditLog(
                session == null ? "system" : session.userId(),
                session == null ? "SYSTEM" : session.role().name(),
                action,
                targetType,
                targetId,
                details);
        repo.save(entry);
        AppLogger.info(LOG, action + " by " + entry.actorId() + " on " + targetType + ":" + targetId);
    }

    /** Records a system-level audit entry (no session). */
    public void recordSystem(String action, String targetType, String targetId, String details) {
        record(null, action, targetType, targetId, details);
    }

    public List<AuditLog> findAll() {
        return repo.findAll();
    }

    public List<AuditLog> findByActor(String actorId) {
        return repo.findByActor(actorId);
    }

    public List<AuditLog> findByAction(String action) {
        return repo.findByAction(action);
    }
}
