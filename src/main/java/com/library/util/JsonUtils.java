package com.library.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public facade over the hand-written JSON codec. Provides type-safe
 * accessors for nested maps and lists produced by the parser.
 *
 * <p>All {@code getX} methods return {@code null} when the key is absent
 * or the value is of the wrong type, so callers can use Optional or
 * null-checks as they prefer. {@code requireX} methods throw when the
 * key is missing or mistyped, for use in deserialization paths where a
 * missing field indicates a corrupt file.
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    /** Parses JSON text into plain Java types. */
    public static Object parse(String json) {
        return JsonParser.parse(json);
    }

    /** Serializes a plain Java value into JSON text. */
    public static String stringify(Object value) {
        return JsonSerializer.toJson(value);
    }

    /** Pretty-prints JSON text with two-space indentation. */
    public static String prettyPrint(String json) {
        Object value = parse(json);
        return pretty(value, 0);
    }

    /** Parses a JSON array string into a list of maps. */
    public static List<Map<String, Object>> parseArray(String json) {
        return JsonParser.parseArray(json);
    }

    /** Pretty-prints a Java value (List, Map, etc.) as JSON. */
    public static String prettyPrint(Object value) {
        return pretty(value, 0);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object value) {
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return new ArrayList<>();
    }

    public static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public static String requireString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return String.valueOf(v);
    }

    public static Integer getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        return null;
    }

    public static int requireInt(Map<String, Object> map, String key) {
        Integer v = getInt(map, key);
        if (v == null) {
            throw new IllegalArgumentException("Missing required integer field: " + key);
        }
        return v;
    }

    public static Long getLong(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    public static long requireLong(Map<String, Object> map, String key) {
        Long v = getLong(map, key);
        if (v == null) {
            throw new IllegalArgumentException("Missing required long field: " + key);
        }
        return v;
    }

    public static Double getDouble(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }

    public static Boolean getBoolean(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean b) {
            return b;
        }
        return null;
    }

    public static boolean requireBoolean(Map<String, Object> map, String key) {
        Boolean v = getBoolean(map, key);
        if (v == null) {
            throw new IllegalArgumentException("Missing required boolean field: " + key);
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getArray(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof List<?> list) {
            return (List<Object>) list;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getObject(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Map<?, ?> nested) {
            return (Map<String, Object>) nested;
        }
        return new LinkedHashMap<>();
    }

    private static String pretty(Object value, int indent) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Map<?, ?> map) {
            return prettyObject(map, indent);
        }
        if (value instanceof List<?> list) {
            return prettyArray(list, indent);
        }
        if (value instanceof String s) {
            return JsonSerializer.toJson(s);
        }
        return JsonSerializer.toJson(value);
    }

    private static String prettyObject(Map<?, ?> map, int indent) {
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{\n");
        String pad = "  ".repeat(indent + 1);
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append(pad).append(JsonSerializer.toJson(String.valueOf(entry.getKey()))).append(": ");
            sb.append(pretty(entry.getValue(), indent + 1));
        }
        sb.append("\n").append("  ".repeat(indent)).append("}");
        return sb.toString();
    }

    private static String prettyArray(List<?> list, int indent) {
        if (list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[\n");
        String pad = "  ".repeat(indent + 1);
        boolean first = true;
        for (Object element : list) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            sb.append(pad).append(pretty(element, indent + 1));
        }
        sb.append("\n").append("  ".repeat(indent)).append("]");
        return sb.toString();
    }
}
