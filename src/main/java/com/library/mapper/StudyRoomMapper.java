package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.StudyRoom;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link StudyRoom} to and from JSON-ready maps.
 */
public final class StudyRoomMapper implements JsonMappable<StudyRoom> {

    @Override
    public Map<String, Object> toMap(StudyRoom sr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sr.getId());
        m.put("roomName", sr.getRoomName());
        m.put("capacity", sr.getCapacity());
        m.put("branchId", sr.getBranchId());
        m.put("active", sr.isActive());
        return m;
    }

    @Override
    public StudyRoom fromMap(Map<String, Object> m) {
        Boolean active = JsonUtils.getBoolean(m, "active");
        if (active == null) {
            active = true;
        }
        return StudyRoom.builder()
                .id(JsonUtils.requireString(m, "id"))
                .roomName(JsonUtils.getString(m, "roomName"))
                .capacity(JsonUtils.getInt(m, "capacity") == null ? 0 : JsonUtils.getInt(m, "capacity"))
                .branchId(JsonUtils.getString(m, "branchId"))
                .active(active)
                .build();
    }
}
