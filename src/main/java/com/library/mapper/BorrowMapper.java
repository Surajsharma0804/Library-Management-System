package com.library.mapper;

import com.library.enums.BorrowStatus;
import com.library.interfaces.JsonMappable;
import com.library.model.BorrowRecord;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BorrowMapper implements JsonMappable<BorrowRecord> {

    @Override
    public Map<String, Object> toMap(BorrowRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("bookId", r.getBookId());
        m.put("registrationNumber", r.getRegistrationNumber());
        m.put("issueDate", DateUtils.format(r.getIssueDate()));
        m.put("dueDate", DateUtils.format(r.getDueDate()));
        m.put("returnDate", DateUtils.format(r.getReturnDate()));
        m.put("renewCount", r.getRenewCount());
        m.put("finePaise", r.getFinePaise());
        m.put("issuedBy", r.getIssuedBy());
        m.put("receivedBy", r.getReceivedBy());
        m.put("status", r.getStatus().name());
        m.put("remarks", r.getRemarks());
        m.put("branchId", r.getBranchId());
        m.put("createdAt", DateUtils.formatDateTime(r.getCreatedAt()));
        m.put("updatedAt", DateUtils.formatDateTime(r.getUpdatedAt()));
        return m;
    }

    @Override
    public BorrowRecord fromMap(Map<String, Object> m) {
        return BorrowRecord.builder()
                .id(JsonUtils.requireString(m, "id"))
                .bookId(JsonUtils.requireString(m, "bookId"))
                .registrationNumber(JsonUtils.requireString(m, "registrationNumber"))
                .issueDate(DateUtils.parseDate(JsonUtils.requireString(m, "issueDate")))
                .dueDate(DateUtils.parseDate(JsonUtils.requireString(m, "dueDate")))
                .returnDate(DateUtils.parseDate(JsonUtils.getString(m, "returnDate")))
                .renewCount(JsonUtils.getInt(m, "renewCount") == null ? 0 : JsonUtils.getInt(m, "renewCount"))
                .finePaise(JsonUtils.getLong(m, "finePaise") == null ? 0L : JsonUtils.getLong(m, "finePaise"))
                .issuedBy(JsonUtils.getString(m, "issuedBy"))
                .receivedBy(JsonUtils.getString(m, "receivedBy"))
                .status(BorrowStatus.fromString(JsonUtils.requireString(m, "status")))
                .remarks(JsonUtils.getString(m, "remarks"))
                .branchId(JsonUtils.getString(m, "branchId"))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .updatedAt(DateUtils.parseDateTime(JsonUtils.getString(m, "updatedAt")))
                .build();
    }
}
