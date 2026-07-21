package com.library.security;

import com.library.enums.UserRole;
import com.library.exception.UnauthorizedAccessException;
import com.library.model.User;
import com.library.util.AppLogger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session store. Maps opaque tokens to {@link Session}s and
 * provides the current-session lookup used by controllers to enforce
 * role-based access control. Sessions expire after {@value #SESSION_TIMEOUT_MINUTES}
 * minutes of inactivity.
 */
public final class SessionManager {

    private static final String LOG = "SessionManager";
    private static final long SESSION_TIMEOUT_MINUTES = 30;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    /** Creates and stores a new session for the given user. */
    public Session create(User user) {
        Session session = Session.forUser(user);
        sessions.put(session.token(), session);
        AppLogger.info(LOG, "Session created for user " + user.getUsername() + " (" + user.getRole() + ")");
        return session;
    }

    /** Returns the session for a token, refreshing last-accessed time. */
    public Optional<Session> lookup(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Session session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (isExpired(session)) {
            sessions.remove(token);
            AppLogger.info(LOG, "Session expired for user " + session.username());
            return Optional.empty();
        }
        Session refreshed = session.withAccessedNow();
        sessions.put(token, refreshed);
        return Optional.of(refreshed);
    }

    /** Removes a session, effectively logging the user out. */
    public void invalidate(String token) {
        if (token == null) {
            return;
        }
        Session removed = sessions.remove(token);
        if (removed != null) {
            AppLogger.info(LOG, "Session invalidated for user " + removed.username());
        }
    }

    /** Removes all sessions (used on shutdown or admin force-logout). */
    public void invalidateAll() {
        sessions.clear();
        AppLogger.info(LOG, "All sessions invalidated");
    }

    public boolean isActive(String token) {
        return token != null && sessions.containsKey(token);
    }

    /**
     * Returns the session for a token or throws if the token is invalid.
     *
     * @throws UnauthorizedAccessException when no session matches
     */
    public Session require(String token) {
        return lookup(token)
                .orElseThrow(() -> new UnauthorizedAccessException("No active session. Please log in."));
    }
    public Session validate(String token) {
        Session session = sessions.get(token);
        if (session == null) throw new com.library.exception.ValidationException("Invalid or expired session.");
        if (isExpired(session)) {
            sessions.remove(token);
            throw new com.library.exception.ValidationException("Session has expired. Please log in again.");
        }
        Session refreshed = session.withAccessedNow();
        sessions.put(token, refreshed);
        return refreshed;
    }

    private boolean isExpired(Session session) {
        return Duration.between(session.accessedAt(), LocalDateTime.now()).toMinutes() >= SESSION_TIMEOUT_MINUTES;
    }
}
