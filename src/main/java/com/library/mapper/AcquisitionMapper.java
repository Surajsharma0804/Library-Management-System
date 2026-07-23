package com.library.mapper;

import com.library.enums.AcquisitionStatus;
import com.library.interfaces.JsonMappable;
import com.library.model.Acquisition;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link Acquisition} to and from JSON-ready maps.
 */
public final class AcquisitionMapper implements JsonMappable<Acquisition> {

    @Override
    public Map<String, Object> toMap(Acquisition a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("requestedTitle", a.getRequestedTitle());
        m.put("author", a.getAuthor());
        m.put("isbn", a.getIsbn());
        m.put("quantity", a.getQuantity());
        m.put("estimatedCostPaise", a.getEstimatedCostPaise());
        m.put("requestedBy", a.getRequestedBy());
        m.put("requestedDate", DateUtils.format(a.getRequestedDate()));
        m.put("status", a.getStatus().name());
        m.put("reviewerNotes", a.getReviewerNotes());
        m.put("createdAt", DateUtils.formatDateTime(a.getCreatedAt()));
        m.put("updatedAt", DateUtils.formatDateTime(a.getUpdatedAt()));
        return m;
    }

    @Override
    public Acquisition fromMap(Map<String, Object> m) {
        String statusStr = JsonUtils.getString(m, "status");
        AcquisitionStatus status = statusStr != null ? AcquisitionStatus.valueOf(statusStr) : AcquisitionStatus.PENDING;

        return Acquisition.builder()
                .id(JsonUtils.requireString(m, "id"))
                .requestedTitle(JsonUtils.getString(m, "requestedTitle"))
                .author(JsonUtils.getString(m, "author"))
                .isbn(JsonUtils.getString(m, "isbn"))
                .quantity(JsonUtils.getInt(m, "quantity") == null ? 0 : JsonUtils.getInt(m, "quantity"))
                .estimatedCostPaise(JsonUtils.getLong(m, "estimatedCostPaise") == null ? 0L : JsonUtils.getLong(m, "estimatedCostPaise"))
                .requestedBy(JsonUtils.requireString(m, "requestedBy"))
                .requestedDate(DateUtils.parseDate(JsonUtils.getString(m, "requestedDate")))
                .status(status)
                .reviewerNotes(JsonUtils.getString(m, "reviewerNotes"))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .updatedAt(DateUtils.parseDateTime(JsonUtils.getString(m, "updatedAt")))
                .build();
    }
}
