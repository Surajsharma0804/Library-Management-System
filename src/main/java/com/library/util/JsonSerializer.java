package com.library.util;

import java.util.List;
import java.util.Map;

final class JsonSerializer {
    private JsonSerializer() {}

    static String toJson(Object value) {
        StringBuilder sb = new StringBuilder();
        serialize(value, sb);
        return sb.toString();
    }

    private static void serialize(Object v, StringBuilder sb) {
        if (v == null) { sb.append("null"); return; }
        if (v instanceof String s) { serializeString(s, sb); return; }
        if (v instanceof Boolean b) { sb.append(b); return; }
        if (v instanceof Number n) { sb.append(n); return; }
        if (v instanceof Map<?, ?> m) { serializeMap(m, sb); return; }
        if (v instanceof List<?> l) { serializeList(l, sb); return; }
        if (v instanceof Object[] arr) { serializeArray(arr, sb); return; }
        serializeString(String.valueOf(v), sb);
    }

    private static void serializeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                case '\r' -> sb.append("\\r");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    private static void serializeMap(Map<?, ?> m, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> e : m.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            serializeString(String.valueOf(e.getKey()), sb);
            sb.append(':');
            serialize(e.getValue(), sb);
        }
        sb.append('}');
    }

    private static void serializeList(List<?> l, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object o : l) {
            if (!first) sb.append(',');
            first = false;
            serialize(o, sb);
        }
        sb.append(']');
    }

    private static void serializeArray(Object[] arr, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object o : arr) {
            if (!first) sb.append(',');
            first = false;
            serialize(o, sb);
        }
        sb.append(']');
    }
}
