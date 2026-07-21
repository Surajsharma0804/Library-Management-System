package com.library.security;

import com.library.model.User;

/**
 * Manages authentication: login, logout, and session creation.
 */
public final class AuthenticationManager {

    private final SessionManager sessionManager;

    public AuthenticationManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public String login(User user, String password) {
        if (user == null || !user.isActive()) {
            throw new com.library.exception.ValidationException("Invalid credentials.");
        }
        if (!PasswordHasher.verify(user.getPasswordHash(), password)) {
            throw new com.library.exception.ValidationException("Invalid credentials.");
        }
        Session session = sessionManager.create(user);
        return session.token();
    }

    public void logout(String token) {
        sessionManager.invalidate(token);
    }

    public Session currentSession(String token) {
        return sessionManager.validate(token);
    }
}
