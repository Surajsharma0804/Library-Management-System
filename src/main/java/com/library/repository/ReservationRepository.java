package com.library.repository;

import com.library.config.Constants;
import com.library.enums.ReservationStatus;
import com.library.mapper.ReservationMapper;
import com.library.model.Reservation;

import java.util.List;

/**
 * JSON-backed repository for {@link Reservation} entities.
 */
public final class ReservationRepository extends JsonRepository<Reservation, String> {

    public ReservationRepository() {
        super(Constants.RESERVATIONS_FILE, new ReservationMapper(), Reservation::getId);
    }

    public List<Reservation> findByBookId(String bookId) {
        return findAll(r -> bookId != null && bookId.equals(r.getBookId()));
    }

    public List<Reservation> findPendingByBookId(String bookId) {
        return findAll(r -> bookId != null && bookId.equals(r.getBookId())
                && r.getStatus() == ReservationStatus.PENDING);
    }

    public List<Reservation> findByRegistrationNumber(String reg) {
        return findAll(r -> reg != null && reg.equals(r.getRegistrationNumber()));
    }

    public List<Reservation> findActiveByRegistrationNumber(String reg) {
        return findAll(r -> reg != null && reg.equals(r.getRegistrationNumber())
                && (r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.READY));
    }

    public List<Reservation> findAllPending() {
        return findAll(r -> r.getStatus() == ReservationStatus.PENDING);
    }
}
