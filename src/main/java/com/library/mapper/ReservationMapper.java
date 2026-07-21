package com.library.mapper;

import com.library.enums.ReservationStatus;
import com.library.interfaces.JsonMappable;
import com.library.model.Reservation;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ReservationMapper implements JsonMappable<Reservation> {

    @Override
    public Map<String, Object> toMap(Reservation r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("bookId", r.getBookId());
        m.put("registrationNumber", r.getRegistrationNumber());
        m.put("reservationDate", DateUtils.format(r.getReservationDate()));
        m.put("expiryDate", DateUtils.format(r.getExpiryDate()));
        m.put("queuePosition", r.getQueuePosition());
        m.put("status", r.getStatus().name());
        m.put("createdAt", DateUtils.formatDateTime(r.getCreatedAt()));
        m.put("updatedAt", DateUtils.formatDateTime(r.getUpdatedAt()));
        return m;
    }

    @Override
    public Reservation fromMap(Map<String, Object> m) {
        return Reservation.builder()
                .id(JsonUtils.requireString(m, "id"))
                .bookId(JsonUtils.requireString(m, "bookId"))
                .registrationNumber(JsonUtils.requireString(m, "registrationNumber"))
                .reservationDate(DateUtils.parseDate(JsonUtils.requireString(m, "reservationDate")))
                .expiryDate(DateUtils.parseDate(JsonUtils.getString(m, "expiryDate")))
                .queuePosition(JsonUtils.getInt(m, "queuePosition") == null ? 0 : JsonUtils.getInt(m, "queuePosition"))
                .status(ReservationStatus.fromString(JsonUtils.requireString(m, "status")))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .updatedAt(DateUtils.parseDateTime(JsonUtils.getString(m, "updatedAt")))
                .build();
    }
}
