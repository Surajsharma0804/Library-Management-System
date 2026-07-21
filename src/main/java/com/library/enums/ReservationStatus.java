package com.library.enums;

/**
 * Lifecycle state of a book reservation.
 */
public enum ReservationStatus {
    /** Waiting in the reservation queue for an available copy. */
    PENDING,
    /** A copy became available and is held for the member until expiry. */
    READY,
    /** Member collected the reserved book (converted to a borrow). */
    FULFILLED,
    /** Reservation expired or was cancelled before fulfilment. */
    CANCELLED,
    /** Reservation expired because the member did not collect in time. */
    EXPIRED;

    public static ReservationStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Reservation status cannot be null");
        }
        return ReservationStatus.valueOf(value.trim().toUpperCase());
    }
}
