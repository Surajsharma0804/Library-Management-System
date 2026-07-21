package com.library.enums;

/**
 * Lifecycle state of a single borrow transaction.
 */
public enum BorrowStatus {
    /** Book issued to a member, not yet returned. */
    ACTIVE,
    /** Book returned on or before the due date. */
    RETURNED,
    /** Returned after the due date; a fine may have been applied. */
    RETURNED_LATE,
    /** Book declared lost by the member or librarian. */
    LOST,
    /** Borrow record cancelled before issue (e.g. reservation fulfilment failed). */
    CANCELLED;

    public static BorrowStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Borrow status cannot be null");
        }
        return BorrowStatus.valueOf(value.trim().toUpperCase());
    }
}
