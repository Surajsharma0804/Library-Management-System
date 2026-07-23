package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.ReadingList;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps {@link ReadingList} to and from JSON-ready maps.
 */
public final class ReadingListMapper implements JsonMappable<ReadingList> {

    @Override
    public Map<String, Object> toMap(ReadingList rl) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rl.getId());
        m.put("registrationNumber", rl.getRegistrationNumber());
        m.put("listName", rl.getListName());
        m.put("description", rl.getDescription());
        m.put("bookIds", rl.getBookIds());   // already List<String>
        m.put("createdAt", DateUtils.formatDateTime(rl.getCreatedAt()));
        m.put("updatedAt", DateUtils.formatDateTime(rl.getUpdatedAt()));
        return m;
    }

    @Override
    public ReadingList fromMap(Map<String, Object> m) {
        List<Object> rawIds = JsonUtils.getArray(m, "bookIds");
        List<String> bookIds = new ArrayList<>(rawIds.size());
        for (Object o : rawIds) {
            if (o != null) bookIds.add((String) o);
        }

        return ReadingList.builder()
                .id(JsonUtils.requireString(m, "id"))
                .registrationNumber(JsonUtils.requireString(m, "registrationNumber"))
                .listName(JsonUtils.getString(m, "listName"))
                .description(JsonUtils.getString(m, "description"))
                .bookIds(bookIds)
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .updatedAt(DateUtils.parseDateTime(JsonUtils.getString(m, "updatedAt")))
                .build();
    }
}
