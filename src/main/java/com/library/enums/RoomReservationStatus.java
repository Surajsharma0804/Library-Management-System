package com.library.enums;

/**
 * Lifecycle state of a room reservation.
 */
public enum RoomReservationStatus {
    /** Room reservation has been confirmed and the room is booked. */
    CONFIRMED,
    /** Room reservation was cancelled before or after confirmation. */
    CANCELLED;

    public static RoomReservationStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Room reservation status cannot be null");
        }
        return RoomReservationStatus.valueOf(value.trim().toUpperCase());
    }
}
