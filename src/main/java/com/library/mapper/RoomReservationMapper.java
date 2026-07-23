package com.library.mapper;

import com.library.enums.RoomReservationStatus;
import com.library.interfaces.JsonMappable;
import com.library.model.RoomReservation;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link RoomReservation} to and from JSON-ready maps.
 */
public final class RoomReservationMapper implements JsonMappable<RoomReservation> {

    @Override
    public Map<String, Object> toMap(RoomReservation rr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rr.getId());
        m.put("roomId", rr.getRoomId());
        m.put("registrationNumber", rr.getRegistrationNumber());
        m.put("date", DateUtils.format(rr.getDate()));
        m.put("startTime", rr.getStartTime());
        m.put("endTime", rr.getEndTime());
        m.put("status", rr.getStatus().name());
        m.put("createdAt", DateUtils.formatDateTime(rr.getCreatedAt()));
        return m;
    }

    @Override
    public RoomReservation fromMap(Map<String, Object> m) {
        String statusStr = JsonUtils.getString(m, "status");
        RoomReservationStatus status = statusStr != null
                ? RoomReservationStatus.valueOf(statusStr)
                : RoomReservationStatus.CONFIRMED;

        return RoomReservation.builder()
                .id(JsonUtils.requireString(m, "id"))
                .roomId(JsonUtils.requireString(m, "roomId"))
                .registrationNumber(JsonUtils.requireString(m, "registrationNumber"))
                .date(DateUtils.parseDate(JsonUtils.requireString(m, "date")))
                .startTime(JsonUtils.getString(m, "startTime"))
                .endTime(JsonUtils.getString(m, "endTime"))
                .status(status)
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .build();
    }
}
