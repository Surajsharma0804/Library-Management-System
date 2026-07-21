package com.library.security;

import com.library.enums.UserRole;
import com.library.exception.UnauthorizedAccessException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Role-based access control gate. Centralises the mapping from
 * permissions to the roles allowed to exercise them, so controllers
 * can call {@link #require(Session, String)} without duplicating
 * authorization logic.
 *
 * <p>Permission strings are constants in {@link Permissions}. Each
 * permission declares which roles may perform it; librarian
 * permissions are additionally gated by per-librarian grants stored
 * on the {@link com.library.model.Librarian} model.
 */
public final class AuthorizationManager {

    /**
     * Require the current session's user to hold the given permission.
     *
     * @throws UnauthorizedAccessException when the user lacks it
     */
    public void require(Session session, String permission) {
        if (session == null) {
            throw new UnauthorizedAccessException("Authentication required.");
        }
        Set<UserRole> allowed = Permissions.rolesFor(permission);
        if (allowed == null) {
            throw new UnauthorizedAccessException("Unknown permission: " + permission);
        }
        if (!allowed.contains(session.role())) {
            throw new UnauthorizedAccessException(
                    "Role " + session.role() + " is not permitted to perform: " + permission);
        }
    }

    /** Convenience: require the session to hold any of the listed roles. */
    public void requireRole(Session session, UserRole... roles) {
        if (session == null) {
            throw new UnauthorizedAccessException("Authentication required.");
        }
        EnumSet<UserRole> required = EnumSet.noneOf(UserRole.class);
        for (UserRole r : roles) {
            required.add(r);
        }
        if (!required.contains(session.role())) {
            throw new UnauthorizedAccessException(
                    "This action requires one of: " + required);
        }
    }

    /** Returns true when the session's role is permitted the permission. */
    public boolean can(Session session, String permission) {
        if (session == null) {
            return false;
        }
        Set<UserRole> allowed = Permissions.rolesFor(permission);
        return allowed != null && allowed.contains(session.role());
    }
}
