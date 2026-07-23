package com.library.enums;

/**
 * Direction of an Inter-Library Loan (ILL) request relative to this library.
 */
public enum ILLDirection {
    /** This library is borrowing a resource from another library. */
    INBOUND,
    /** This library is lending a resource to another library. */
    OUTBOUND;

    public static ILLDirection fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ILL direction cannot be null");
        }
        return ILLDirection.valueOf(value.trim().toUpperCase());
    }
}
