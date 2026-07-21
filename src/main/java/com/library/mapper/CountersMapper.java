package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.Counters;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CountersMapper implements JsonMappable<Counters> {
    @Override
    public Map<String, Object> toMap(Counters c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "counters");
        m.put("values", new HashMap<>(c.getValues()));
        return m;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Counters fromMap(Map<String, Object> m) {
        Counters c = new Counters();
        Object v = m.get("values");
        if (v instanceof Map<?, ?> raw) {
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                c.set(String.valueOf(e.getKey()), ((Number) e.getValue()).longValue());
            }
        }
        return c;
    }
}
