package com.library.enums;

/**
 * Membership lifecycle state of a library member (student).
 */
public enum MembershipStatus {
    ACTIVE,
    INACTIVE,
    EXPIRED,
    BLOCKED;

    /**
     * Parses a status from a string, case-insensitively.
     *
     * @param value raw status text
     * @return matching enum constant
     * @throws IllegalArgumentException if no constant matches
     */
    public static MembershipStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Membership status cannot be null");
        }
        return MembershipStatus.valueOf(value.trim().toUpperCase());
    }

    /**
     * @return true when a member in this status is permitted to borrow books.
     */
    public boolean canBorrow() {
        return this == ACTIVE;
    }
}
