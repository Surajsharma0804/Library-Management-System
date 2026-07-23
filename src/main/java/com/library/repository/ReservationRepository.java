package com.library.repository;

import com.library.config.Constants;
import com.library.enums.ReservationStatus;
import com.library.mapper.ReservationMapper;
import com.library.model.Reservation;

import java.util.List;

/**
 * JSON-backed repository for {@link Reservation} entities.
 * Uses secondary indexes for O(1) lookups by bookId and registrationNumber.
 */
public final class ReservationRepository extends IndexedRepository<Reservation, String> {

    public ReservationRepository() {
        super(Constants.RESERVATIONS_FILE, new ReservationMapper(), Reservation::getId);
        registerSecondaryIndex("bookId");
        registerSecondaryIndex("registrationNumber");
    }

    @Override
    protected String secondaryKey(String indexName, Reservation entity) {
        return switch (indexName) {
            case "bookId"             -> entity.getBookId();
            case "registrationNumber" -> entity.getRegistrationNumber();
            default                   -> null;
        };
    }

    public List<Reservation> findByBookId(String bookId) {
        return findAllBySecondaryKey("bookId", bookId);
    }

    public List<Reservation> findPendingByBookId(String bookId) {
        return findAllBySecondaryKey("bookId", bookId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .toList();
    }

    public List<Reservation> findByRegistrationNumber(String reg) {
        return findAllBySecondaryKey("registrationNumber", reg);
    }

    public List<Reservation> findActiveByRegistrationNumber(String reg) {
        return findAllBySecondaryKey("registrationNumber", reg).stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.READY)
                .toList();
    }

    public List<Reservation> findAllPending() {
        return findAll(r -> r.getStatus() == ReservationStatus.PENDING);
    }
}
