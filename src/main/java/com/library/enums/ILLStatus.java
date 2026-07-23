package com.library.enums;

/**
 * Lifecycle state of an Inter-Library Loan (ILL) transaction.
 */
public enum ILLStatus {
    /** ILL request has been submitted and is awaiting confirmation from the partner library. */
    REQUESTED,
    /** ILL has been confirmed and the resource is in transit or in use. */
    ACTIVE,
    /** Resource has been returned to the originating library. */
    RETURNED,
    /** ILL request was cancelled before or during fulfilment. */
    CANCELLED;

    public static ILLStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ILL status cannot be null");
        }
        return ILLStatus.valueOf(value.trim().toUpperCase());
    }
}
