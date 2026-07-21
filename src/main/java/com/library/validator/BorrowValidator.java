package com.library.validator;

import com.library.model.BorrowRecord;
import com.library.model.Student;

/**
 * Validator for borrow-related business rules.
 */
public final class BorrowValidator {
    private BorrowValidator() {}

    public static boolean canBorrow(Student student, int activeBorrows, int maxBorrows) {
        if (student == null) return false;
        if (student.membershipExpired()) return false;
        if (student.getFineBalancePaise() > 0) return false;
        return activeBorrows < maxBorrows;
    }

    public static boolean canRenew(BorrowRecord record, int maxRenewals, boolean hasPendingReservation) {
        if (record == null) return false;
        if (record.getStatus() != com.library.enums.BorrowStatus.ACTIVE) return false;
        if (record.getRenewCount() >= maxRenewals) return false;
        return !hasPendingReservation;
    }

    public static boolean isOverdue(BorrowRecord record) {
        return record != null && record.isOverdue();
    }
}
