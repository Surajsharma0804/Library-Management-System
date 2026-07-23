package com.library.pbt;

import com.library.enums.BookStatus;
import com.library.model.Book;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.List;

/**
 * Property-based test: Book quantity invariant.
 *
 * After any sequence of quantity-mutating operations:
 *   available + reserved + borrowed == total
 *   and all counters >= 0
 *
 * **Validates: Requirements 3.1** (inventory consistency)
 */
class BookQuantityInvariantTest {

    /** Possible operations on a Book's quantity: 0=issued, 1=returned, 2=reserved, 3=release */
    @Provide
    Arbitrary<List<Integer>> opSequences() {
        return Arbitraries.integers().between(0, 3).list().ofMinSize(1).ofMaxSize(20);
    }

    @Property
    void quantityInvariantHoldsAfterAnyOperationSequence(
            @ForAll @IntRange(min = 3, max = 20) int totalQuantity,
            @ForAll("opSequences") List<Integer> ops
    ) {
        Book book = Book.builder()
                .id("BK-TEST")
                .isbn("978-0-000-00000-0")
                .title("Invariant Test Book")
                .author("Test Author")
                .totalQuantity(totalQuantity)
                .availableQuantity(totalQuantity)
                .reservedQuantity(0)
                .status(BookStatus.AVAILABLE)
                .build();

        for (int op : ops) {
            try {
                switch (op) {
                    case 0 -> book.markIssued();
                    case 1 -> book.markReturned();
                    case 2 -> book.markReserved();
                    case 3 -> book.releaseReservation();
                }
            } catch (IllegalStateException e) {
                // Normal: operation not valid in current state, skip it
            }

            // Invariant: available + reserved + borrowed == total, all >= 0
            int available = book.getAvailableQuantity();
            int reserved  = book.getReservedQuantity();
            int borrowed  = book.getBorrowedQuantity();
            int total     = book.getTotalQuantity();

            assert available >= 0 :
                    "availableQuantity < 0: " + available;
            assert reserved >= 0 :
                    "reservedQuantity < 0: " + reserved;
            assert borrowed >= 0 :
                    "borrowedQuantity < 0: " + borrowed;
            assert available + reserved + borrowed == total :
                    "Invariant broken: " + available + " + " + reserved + " + " + borrowed + " != " + total;
        }
    }
}
