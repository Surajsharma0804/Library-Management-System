package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.Branch;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link Branch} to and from JSON-ready maps.
 */
public final class BranchMapper implements JsonMappable<Branch> {

    @Override
    public Map<String, Object> toMap(Branch b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("branchName", b.getBranchName());
        m.put("location", b.getLocation());
        m.put("phone", b.getPhone());
        m.put("createdAt", DateUtils.formatDateTime(b.getCreatedAt()));
        return m;
    }

    @Override
    public Branch fromMap(Map<String, Object> m) {
        return Branch.builder()
                .id(JsonUtils.requireString(m, "id"))
                .branchName(JsonUtils.requireString(m, "branchName"))
                .location(JsonUtils.getString(m, "location"))
                .phone(JsonUtils.getString(m, "phone"))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .build();
    }
}
