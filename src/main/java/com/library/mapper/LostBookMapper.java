package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.LostBookRecord;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link LostBookRecord} to and from JSON-ready maps.
 */
public final class LostBookMapper implements JsonMappable<LostBookRecord> {

    @Override
    public Map<String, Object> toMap(LostBookRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("borrowRecordId", r.getBorrowRecordId());
        m.put("bookId", r.getBookId());
        m.put("registrationNumber", r.getRegistrationNumber());
        m.put("replacementCostPaise", r.getReplacementCostPaise());
        m.put("reportedDate", DateUtils.format(r.getReportedDate()));
        m.put("notes", r.getNotes());
        m.put("createdAt", DateUtils.formatDateTime(r.getCreatedAt()));
        return m;
    }

    @Override
    public LostBookRecord fromMap(Map<String, Object> m) {
        return LostBookRecord.builder()
                .id(JsonUtils.requireString(m, "id"))
                .borrowRecordId(JsonUtils.requireString(m, "borrowRecordId"))
                .bookId(JsonUtils.requireString(m, "bookId"))
                .registrationNumber(JsonUtils.requireString(m, "registrationNumber"))
                .replacementCostPaise(JsonUtils.getLong(m, "replacementCostPaise") == null ? 0L
                        : JsonUtils.getLong(m, "replacementCostPaise"))
                .reportedDate(DateUtils.parseDate(JsonUtils.getString(m, "reportedDate")))
                .notes(JsonUtils.getString(m, "notes"))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .build();
    }
}
