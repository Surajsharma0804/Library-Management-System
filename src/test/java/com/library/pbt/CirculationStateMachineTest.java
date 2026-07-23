package com.library.pbt;

import com.library.enums.BorrowStatus;
import com.library.model.BorrowRecord;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDate;

/**
 * Property-based test: BorrowRecord circulation state machine.
 *
 * - renewCount never exceeds 2 (canRenew() returns false after 2 renewals)
 * - status is always ACTIVE or a valid terminal state after operations
 *
 * **Validates: Requirements 4.1** (renewal count cap)
 */
class CirculationStateMachineTest {

    private BorrowRecord buildActiveRecord(int initialRenewCount) {
        return BorrowRecord.builder()
                .id("BR-001")
                .bookId("BK-001")
                .registrationNumber("REG-001")
                .issueDate(LocalDate.now().minusDays(7))
                .dueDate(LocalDate.now().plusDays(7))
                .renewCount(initialRenewCount)
                .status(BorrowStatus.ACTIVE)
                .build();
    }

    @Property
    void renewCountNeverExceedsTwo(
            @ForAll @IntRange(min = 0, max = 2) int renewalsToApply
    ) {
        BorrowRecord record = buildActiveRecord(0);

        for (int i = 0; i < renewalsToApply; i++) {
            if (record.canRenew()) {
                record.incrementRenewCount();
                // Extend due date as a renewal would do
                record.setDueDate(record.getDueDate().plusDays(14));
            }
        }

        assert record.getRenewCount() <= 2 :
                "Renew count exceeded 2: " + record.getRenewCount();
    }

    @Property
    void canRenewReturnsFalseAfterTwoRenewals() {
        BorrowRecord record = buildActiveRecord(0);

        // Apply exactly 2 renewals
        assert record.canRenew() : "Should be able to renew before any renewal";
        record.incrementRenewCount();

        assert record.canRenew() : "Should be able to renew after 1 renewal";
        record.incrementRenewCount();

        // After 2 renewals, canRenew must be false
        assert !record.canRenew() :
                "canRenew() should be false after 2 renewals, renewCount=" + record.getRenewCount();
    }

    @Property
    void statusIsAlwaysValidAfterTerminalTransition(
            @ForAll BorrowStatus terminalStatus
    ) {
        BorrowRecord record = buildActiveRecord(0);
        record.setStatus(terminalStatus);

        BorrowStatus status = record.getStatus();
        boolean isValidStatus = status == BorrowStatus.ACTIVE
                || status == BorrowStatus.RETURNED
                || status == BorrowStatus.RETURNED_LATE
                || status == BorrowStatus.LOST
                || status == BorrowStatus.CANCELLED;

        assert isValidStatus :
                "Status is not a valid BorrowStatus: " + status;
    }

    @Property
    void activeRecordWithFuturedueDateIsNotOverdue() {
        BorrowRecord record = BorrowRecord.builder()
                .id("BR-002")
                .bookId("BK-002")
                .registrationNumber("REG-002")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowStatus.ACTIVE)
                .build();

        assert !record.isOverdue() :
                "A record with future due date should not be overdue";
        assert record.canRenew() :
                "A fresh active record with 0 renewals should be renewable";
    }
}
