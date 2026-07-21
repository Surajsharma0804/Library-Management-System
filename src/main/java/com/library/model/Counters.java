package com.library.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Persistent counter store for ID generation.
 */
public final class Counters {
    private final Map<String, Long> values = new HashMap<>();
    public long incrementAndGet(String key) { long n = values.getOrDefault(key, 0L) + 1; values.put(key, n); return n; }
    public long get(String key) { return values.getOrDefault(key, 0L); }
    public void set(String key, long v) { values.put(key, v); }
    public Map<String, Long> getValues() { return values; }
}
