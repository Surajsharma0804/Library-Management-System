package com.library.mapper;

import com.library.enums.FineStatus;
import com.library.interfaces.JsonMappable;
import com.library.model.Fine;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FineMapper implements JsonMappable<Fine> {
    @Override
    public Map<String, Object> toMap(Fine f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("registrationNumber", f.getRegistrationNumber());
        m.put("borrowId", f.getBorrowId());
        m.put("bookId", f.getBookId());
        m.put("amountPaise", f.getAmountPaise());
        m.put("reason", f.getReason());
        m.put("status", f.getStatus().name());
        m.put("createdBy", f.getCreatedBy());
        m.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);
        m.put("paidAt", f.getSettledAt() != null ? f.getSettledAt().toString() : null);
        return m;
    }

    @Override
    public Fine fromMap(Map<String, Object> m) {
        return Fine.builder()
                .id((String) m.get("id"))
                .registrationNumber((String) m.get("registrationNumber"))
                .borrowId((String) m.get("borrowId"))
                .bookId((String) m.get("bookId"))
                .amountPaise(((Number) m.get("amountPaise")).longValue())
                .reason((String) m.get("reason"))
                .status(FineStatus.valueOf((String) m.get("status")))
                .createdBy((String) m.get("createdBy"))
                .build();
    }
}
