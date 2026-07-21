package com.library.enums;

/**
 * Lifecycle status of a physical book copy / inventory entry.
 */
public enum BookStatus {
    AVAILABLE,
    BORROWED,
    RESERVED,
    LOST,
    DAMAGED,
    UNDER_REPAIR,
    ARCHIVED;

    /**
     * Parses a status from a string, case-insensitively.
     *
     * @param value raw status text
     * @return matching enum constant
     * @throws IllegalArgumentException if no constant matches
     */
    public static BookStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Book status cannot be null");
        }
        return BookStatus.valueOf(value.trim().toUpperCase());
    }

    /**
     * @return true when a book in this status can be issued to a borrower.
     */
    public boolean isIssuable() {
        return this == AVAILABLE;
    }
}
