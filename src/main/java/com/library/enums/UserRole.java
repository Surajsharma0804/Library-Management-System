package com.library.enums;

/**
 * Coarse-grained system role used by the role-based access control layer.
 */
public enum UserRole {
    ADMIN,
    LIBRARIAN,
    STUDENT;

    /**
     * Parses a role from a string, case-insensitively.
     *
     * @param value raw role text
     * @return matching enum constant
     * @throws IllegalArgumentException if no constant matches
     */
    public static UserRole fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
