package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.RoomReservationMapper;
import com.library.model.RoomReservation;

import java.time.LocalDate;
import java.util.List;

/**
 * JSON-backed repository for {@link RoomReservation} entities.
 * Uses secondary indexes for O(1) lookups by roomId and registrationNumber.
 */
public final class RoomReservationRepository extends IndexedRepository<RoomReservation, String> {

    public RoomReservationRepository() {
        super(Constants.ROOM_RESERVATIONS_FILE, new RoomReservationMapper(), RoomReservation::getId);
        registerSecondaryIndex("roomId");
        registerSecondaryIndex("registrationNumber");
    }

    @Override
    protected String secondaryKey(String indexName, RoomReservation entity) {
        return switch (indexName) {
            case "roomId"             -> entity.getRoomId();
            case "registrationNumber" -> entity.getRegistrationNumber();
            default                   -> null;
        };
    }

    /**
     * Returns all reservations for the given room.
     *
     * @param roomId the study room identifier; returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link RoomReservation}s
     */
    public List<RoomReservation> findByRoomId(String roomId) {
        if (roomId == null) {
            return List.of();
        }
        return findAllBySecondaryKey("roomId", roomId);
    }

    /**
     * Returns all reservations made by the given student.
     *
     * @param reg the student registration number; returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link RoomReservation}s
     */
    public List<RoomReservation> findByRegistrationNumber(String reg) {
        if (reg == null) {
            return List.of();
        }
        return findAllBySecondaryKey("registrationNumber", reg);
    }

    /**
     * Returns all reservations for the given room on the specified date.
     * Performs an O(k) scan over the room's reservation bucket, where k is the
     * number of reservations for that room.
     *
     * @param roomId the study room identifier; returns an empty list if {@code null}
     * @param date   the reservation date; returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link RoomReservation}s
     */
    public List<RoomReservation> findByRoomAndDate(String roomId, LocalDate date) {
        if (roomId == null || date == null) {
            return List.of();
        }
        return findAllBySecondaryKey("roomId", roomId).stream()
                .filter(r -> date.equals(r.getDate()))
                .toList();
    }
}
