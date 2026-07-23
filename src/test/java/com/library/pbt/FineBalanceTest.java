package com.library.pbt;

import com.library.enums.MembershipStatus;
import com.library.model.Student;
import com.library.security.PasswordHasher;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.util.List;

/**
 * Property-based test: Student fine balance is never negative.
 *
 * **Validates: Requirements 5.2** (fine balance invariant)
 */
class FineBalanceTest {

    /** Generates a list of fine amounts (0-10000 paise each) */
    @Provide
    Arbitrary<List<Long>> fineAmounts() {
        return Arbitraries.longs().between(0L, 10000L).list().ofMinSize(1).ofMaxSize(30);
    }

    /** Generates a list of operations: 0=ADD, 1=SUBTRACT */
    @Provide
    Arbitrary<List<Integer>> fineOps() {
        return Arbitraries.integers().between(0, 1).list().ofMinSize(1).ofMaxSize(30);
    }

    private Student buildStudent(long initialFine) {
        return Student.builder()
                .id("S-001")
                .username("teststudent")
                .firstName("Test")
                .lastName("Student")
                .passwordHash(PasswordHasher.hash("Password123"))
                .registrationNumber("REG-001")
                .libraryCardNumber("CARD-001")
                .fineBalancePaise(initialFine)
                .membershipStatus(MembershipStatus.ACTIVE)
                .borrowLimit(5)
                .build();
    }

    @Property
    void fineBalanceNeverGoesNegative(
            @ForAll @LongRange(min = 0, max = 50000) long initialFine,
            @ForAll("fineAmounts") List<Long> amounts,
            @ForAll("fineOps") List<Integer> ops
    ) {
        Student student = buildStudent(initialFine);

        int limit = Math.min(amounts.size(), ops.size());
        for (int i = 0; i < limit; i++) {
            long amount = amounts.get(i);
            int op = ops.get(i);

            if (op == 0) {
                student.addFine(amount);
            } else {
                student.subtractFine(amount);
            }

            assert student.getFineBalancePaise() >= 0 :
                    "Fine balance went negative: " + student.getFineBalancePaise();
        }
    }

    @Property
    void addFineZeroIsNoOp(
            @ForAll @LongRange(min = 0, max = 100000) long initialFine
    ) {
        Student student = buildStudent(initialFine);
        long before = student.getFineBalancePaise();
        student.addFine(0);
        long after = student.getFineBalancePaise();

        assert before == after :
                "addFine(0) changed balance from " + before + " to " + after;
    }
}
