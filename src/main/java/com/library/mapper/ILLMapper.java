package com.library.mapper;

import com.library.enums.ILLDirection;
import com.library.enums.ILLStatus;
import com.library.interfaces.JsonMappable;
import com.library.model.InterLibraryLoan;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link InterLibraryLoan} to and from JSON-ready maps.
 */
public final class ILLMapper implements JsonMappable<InterLibraryLoan> {

    @Override
    public Map<String, Object> toMap(InterLibraryLoan ill) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ill.getId());
        m.put("direction", ill.getDirection().name());
        m.put("partnerLibraryName", ill.getPartnerLibraryName());
        m.put("bookTitle", ill.getBookTitle());
        m.put("bookIsbn", ill.getBookIsbn());
        m.put("requestedBy", ill.getRequestedBy());
        m.put("requestedDate", DateUtils.format(ill.getRequestedDate()));
        m.put("expectedReturnDate", ill.getExpectedReturnDate() != null ? DateUtils.format(ill.getExpectedReturnDate()) : null);
        m.put("actualReturnDate", ill.getActualReturnDate() != null ? DateUtils.format(ill.getActualReturnDate()) : null);
        m.put("status", ill.getStatus().name());
        m.put("notes", ill.getNotes());
        m.put("createdAt", DateUtils.formatDateTime(ill.getCreatedAt()));
        m.put("updatedAt", DateUtils.formatDateTime(ill.getUpdatedAt()));
        return m;
    }

    @Override
    public InterLibraryLoan fromMap(Map<String, Object> m) {
        String directionStr = JsonUtils.getString(m, "direction");
        ILLDirection direction = directionStr != null ? ILLDirection.valueOf(directionStr) : null;

        String statusStr = JsonUtils.getString(m, "status");
        ILLStatus status = statusStr != null ? ILLStatus.valueOf(statusStr) : null;

        InterLibraryLoan.Builder b = InterLibraryLoan.builder()
                .id(JsonUtils.requireString(m, "id"))
                .direction(direction)
                .partnerLibraryName(JsonUtils.requireString(m, "partnerLibraryName"))
                .bookTitle(JsonUtils.requireString(m, "bookTitle"))
                .bookIsbn(JsonUtils.getString(m, "bookIsbn"))
                .requestedBy(JsonUtils.getString(m, "requestedBy"))
                .requestedDate(DateUtils.parseDate(JsonUtils.getString(m, "requestedDate")))
                .expectedReturnDate(DateUtils.parseDate(JsonUtils.getString(m, "expectedReturnDate")))
                .actualReturnDate(DateUtils.parseDate(JsonUtils.getString(m, "actualReturnDate")))
                .status(status)
                .notes(JsonUtils.getString(m, "notes"))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .updatedAt(DateUtils.parseDateTime(JsonUtils.getString(m, "updatedAt")));

        return b.build();
    }
}
