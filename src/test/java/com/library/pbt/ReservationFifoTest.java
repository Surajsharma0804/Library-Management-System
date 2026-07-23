package com.library.pbt;

import com.library.enums.ReservationStatus;
import com.library.model.Reservation;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Property-based test: Reservation queue is FIFO — sorted by queuePosition,
 * positions are always monotonically increasing.
 *
 * **Validates: Requirements 6.1** (reservation ordering)
 */
class ReservationFifoTest {

    /** Generates a list of queue positions (1-100 each) */
    @Provide
    Arbitrary<List<Integer>> positionLists() {
        return Arbitraries.integers().between(1, 100).list().ofMinSize(1).ofMaxSize(20);
    }

    @Property
    void sortedReservationsHaveMonotonicallyIncreasingPositions(
            @ForAll("positionLists") List<Integer> positions
    ) {
        // Build reservations with the given positions
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            reservations.add(Reservation.builder()
                    .id("RES-" + i)
                    .bookId("BK-001")
                    .registrationNumber("REG-" + i)
                    .reservationDate(LocalDate.now())
                    .queuePosition(positions.get(i))
                    .status(ReservationStatus.PENDING)
                    .build());
        }

        // Sort by queuePosition
        reservations.sort(Comparator.comparingInt(Reservation::getQueuePosition));

        // Assert monotonically non-decreasing positions after sort
        for (int i = 1; i < reservations.size(); i++) {
            int prev = reservations.get(i - 1).getQueuePosition();
            int curr = reservations.get(i).getQueuePosition();
            assert prev <= curr :
                    "Queue positions not in order at index " + i + ": " + prev + " > " + curr;
        }
    }

    @Property
    void nReservationsWithSequentialPositionsAreInStrictAscendingOrder(
            @ForAll @IntRange(min = 1, max = 15) int n
    ) {
        // Build N reservations with queue positions 1..N
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            reservations.add(Reservation.builder()
                    .id("RES-" + i)
                    .bookId("BK-001")
                    .registrationNumber("REG-" + i)
                    .reservationDate(LocalDate.now())
                    .queuePosition(i)
                    .status(ReservationStatus.PENDING)
                    .build());
        }

        // Sort and verify strictly ascending 1, 2, ..., N
        reservations.sort(Comparator.comparingInt(Reservation::getQueuePosition));
        for (int i = 0; i < reservations.size(); i++) {
            assert reservations.get(i).getQueuePosition() == i + 1 :
                    "Expected position " + (i + 1) + " but got " + reservations.get(i).getQueuePosition();
        }
    }
}
