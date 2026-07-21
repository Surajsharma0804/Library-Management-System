package com.library.security;

import com.library.enums.UserRole;
import com.library.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable session record representing an authenticated user session.
 */
public record Session(
        String token,
        String userId,
        String username,
        UserRole role,
        LocalDateTime accessedAt
) {
    public static Session forUser(User user) {
        return new Session(UUID.randomUUID().toString(), user.getId(),
                user.getUsername(), user.getRole(), LocalDateTime.now());
    }

    public Session withAccessedNow() {
        return new Session(token, userId, username, role, LocalDateTime.now());
    }
}
