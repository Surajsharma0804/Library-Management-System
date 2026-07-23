package com.library.service;

import com.library.enums.RoomReservationStatus;
import com.library.enums.UserRole;
import com.library.model.RoomReservation;
import com.library.model.StudyRoom;
import com.library.repository.RoomReservationRepository;
import com.library.repository.StudyRoomRepository;
import com.library.security.AuthorizationManager;
import com.library.security.Permissions;
import com.library.security.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing study room reservations.
 *
 * <p>Supports creating, cancelling, and querying reservations with RBAC enforcement.
 *
 * <p>Requirements: 24.1
 */
public final class RoomReservationService {

    private final StudyRoomRepository roomRepo;
    private final RoomReservationRepository reservationRepo;
    private final AuthorizationManager rbac;

    /**
     * Constructs a {@code RoomReservationService} with all required dependencies.
     *
     * @param roomRepo        repository for study room persistence
     * @param reservationRepo repository for room reservation persistence
     * @param rbac            authorization manager for permission enforcement
     */
    public RoomReservationService(StudyRoomRepository roomRepo,
                                   RoomReservationRepository reservationRepo,
                                   AuthorizationManager rbac) {
        this.roomRepo        = Objects.requireNonNull(roomRepo,        "roomRepo must not be null");
        this.reservationRepo = Objects.requireNonNull(reservationRepo, "reservationRepo must not be null");
        this.rbac            = Objects.requireNonNull(rbac,            "rbac must not be null");
    }

    /**
     * Creates a confirmed room reservation for the given student session.
     *
     * <p>Checks that the room exists and is active, then verifies that the
     * requested time slot does not conflict with any existing CONFIRMED reservation
     * for the same room on the same date.
     *
     * @param session   the authenticated student session; must have
     *                  {@link Permissions#ROOM_RESERVATION_CREATE}
     * @param roomId    the ID of the study room to reserve; must not be {@code null}
     * @param date      the date of the reservation; must not be {@code null}
     * @param startTime start of the requested slot in {@code HH:mm} format
     * @param endTime   end   of the requested slot in {@code HH:mm} format
     * @return the newly created and persisted {@link RoomReservation}
     * @throws NoSuchElementException if the room does not exist or is inactive
     * @throws IllegalStateException  if the time slot is already taken
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *         {@link Permissions#ROOM_RESERVATION_CREATE}
     */
    public RoomReservation reserve(Session session, String roomId, LocalDate date,
                                   String startTime, String endTime) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.ROOM_RESERVATION_CREATE);

        // Validate room exists and is active
        StudyRoom room = roomRepo.findById(roomId).orElse(null);
        if (room == null || !room.isActive()) {
            throw new NoSuchElementException("Study room not found or inactive: " + roomId);
        }

        // Conflict check — overlapping CONFIRMED reservations for same room + date
        List<RoomReservation> existing = reservationRepo.findByRoomAndDate(roomId, date);
        boolean conflict = existing.stream()
                .filter(r -> r.getStatus() == RoomReservationStatus.CONFIRMED)
                .anyMatch(r -> timesOverlap(r.getStartTime(), r.getEndTime(), startTime, endTime));
        if (conflict) {
            throw new IllegalStateException("Time slot not available");
        }

        RoomReservation reservation = RoomReservation.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(RoomReservationStatus.CONFIRMED)
                .registrationNumber(session.username())
                .build();

        reservationRepo.save(reservation);
        return reservation;
    }

    /**
     * Cancels an existing room reservation.
     *
     * <p>The session user must own the reservation, or hold the ADMIN or LIBRARIAN role.
     *
     * @param session       the authenticated session
     * @param reservationId the ID of the reservation to cancel; must not be {@code null}
     * @return the updated and persisted {@link RoomReservation}
     * @throws NoSuchElementException            if no reservation with the given ID exists
     * @throws com.library.exception.UnauthorizedAccessException if the session is neither
     *         the owner nor an ADMIN/LIBRARIAN
     */
    public RoomReservation cancel(Session session, String reservationId) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(reservationId, "reservationId must not be null");

        RoomReservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Room reservation not found: " + reservationId));

        // Ownership check — owner or ADMIN/LIBRARIAN may cancel
        boolean isOwner = session.username().equals(reservation.getRegistrationNumber());
        boolean isStaff = session.role() == UserRole.ADMIN || session.role() == UserRole.LIBRARIAN;
        if (!isOwner && !isStaff) {
            throw new com.library.exception.UnauthorizedAccessException(
                    "You are not permitted to cancel this reservation.");
        }

        reservation.cancel();
        reservationRepo.save(reservation);
        return reservation;
    }

    /**
     * Returns a map of date to list of CONFIRMED reservations for the given room
     * over the 7-day window starting from (and including) {@code from}.
     *
     * @param roomId the ID of the study room; must not be {@code null}
     * @param from   the first date of the availability window; must not be {@code null}
     * @return ordered map with exactly 7 entries (some lists may be empty)
     */
    public Map<LocalDate, List<RoomReservation>> getAvailabilityForRoom(String roomId, LocalDate from) {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(from,   "from must not be null");

        Map<LocalDate, List<RoomReservation>> result = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = from.plusDays(i);
            List<RoomReservation> confirmed = reservationRepo.findByRoomAndDate(roomId, d)
                    .stream()
                    .filter(r -> r.getStatus() == RoomReservationStatus.CONFIRMED)
                    .toList();
            result.put(d, confirmed);
        }
        return result;
    }

    /**
     * Returns all reservations belonging to the session's student account.
     *
     * @param session the authenticated session; must not be {@code null}
     * @return list of {@link RoomReservation}s for the session user, or an empty list
     */
    public List<RoomReservation> findByStudent(Session session) {
        Objects.requireNonNull(session, "session must not be null");
        return reservationRepo.findByRegistrationNumber(session.username());
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Returns {@code true} when the two time intervals overlap.
     *
     * <p>Intervals [s1,e1) and [s2,e2) overlap when {@code s1 < e2 && s2 < e1}.
     * String comparison is valid for {@code HH:mm} formatted times.
     *
     * @param s1 start of first interval
     * @param e1 end   of first interval
     * @param s2 start of second interval
     * @param e2 end   of second interval
     * @return {@code true} iff the intervals overlap
     */
    private boolean timesOverlap(String s1, String e1, String s2, String e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) {
            return false;
        }
        return s1.compareTo(e2) < 0 && s2.compareTo(e1) < 0;
    }
}
