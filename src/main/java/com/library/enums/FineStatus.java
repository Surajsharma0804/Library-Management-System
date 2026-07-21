package com.library.enums;

/**
 * State of a monetary fine applied to a member.
 */
public enum FineStatus {
    /** Fine raised but not yet paid or waived. */
    PENDING,
    /** Fine collected from the member. */
    PAID,
    /** Fine waived by an authorised librarian. */
    WAIVED;

    public static FineStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Fine status cannot be null");
        }
        return FineStatus.valueOf(value.trim().toUpperCase());
    }
}
