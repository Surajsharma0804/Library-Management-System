package com.library.enums;

/**
 * Lifecycle status of a book acquisition request.
 */
public enum AcquisitionStatus {
    PENDING,
    APPROVED,
    REJECTED,
    RECEIVED;

    /**
     * Parses a status from a string, case-insensitively.
     *
     * @param value raw status text
     * @return matching enum constant
     * @throws IllegalArgumentException if no constant matches
     */
    public static AcquisitionStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Acquisition status cannot be null");
        }
        return AcquisitionStatus.valueOf(value.trim().toUpperCase());
    }
}
