package com.library.repository;

import com.library.config.Constants;
import com.library.model.Counters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.library.util.JsonUtils;

/**
 * Repository for the singleton Counters entity used for ID generation.
 */
public final class CountersRepository {
    private final Path filePath = Path.of(Constants.COUNTERS_FILE);

    public String nextId(String prefix) {
        Counters c = load();
        long next = c.incrementAndGet(prefix);
        save(c);
        return prefix + "-" + String.format("%06d", next);
    }

    @SuppressWarnings("unchecked")
    private Counters load() {
        if (!Files.exists(filePath)) return new Counters();
        try {
            String content = Files.readString(filePath);
            if (content.isBlank()) return new Counters();
            List<Map<String, Object>> list = JsonUtils.parseArray(content);
            for (Map<String, Object> m : list) {
                if ("counters".equals(m.get("id"))) {
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
            return new Counters();
        } catch (IOException e) { return new Counters(); }
    }

    private void save(Counters c) {
        try {
            Files.createDirectories(filePath.getParent());
            Map<String, Object> m = new HashMap<>();
            m.put("id", "counters");
            m.put("values", c.getValues());
            Files.writeString(filePath, JsonUtils.prettyPrint(List.of(m)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write counters", e);
        }
    }
}
