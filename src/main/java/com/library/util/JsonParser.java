package com.library.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JsonParser {
    private final String src;
    private int pos;

    private JsonParser(String src) { this.src = src; }

    static Object parse(String json) {
        if (json == null || json.isBlank()) throw new IllegalArgumentException("Empty JSON input");
        JsonParser p = new JsonParser(json.trim());
        p.skipWs();
        Object v = p.parseValue();
        p.skipWs();
        if (p.pos < p.src.length()) throw new IllegalArgumentException("Unexpected trailing content at " + p.pos);
        return v;
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> parseArray(String json) {
        Object v = parse(json);
        if (v instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) result.add((Map<String, Object>) m);
            }
            return result;
        }
        return new ArrayList<>();
    }

    private Object parseValue() {
        skipWs();
        if (pos >= src.length()) return null;
        char c = src.charAt(pos);
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArrayValue();
            case '"' -> parseString();
            case 't', 'f' -> parseBool();
            case 'n' -> parseNull();
            default -> parseNumber();
        };
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> m = new LinkedHashMap<>();
        pos++;
        skipWs();
        if (pos < src.length() && src.charAt(pos) == '}') { pos++; return m; }
        while (true) {
            skipWs();
            if (pos >= src.length() || src.charAt(pos) != '"')
                throw new IllegalArgumentException("Expected string key at " + pos);
            String key = parseString();
            skipWs();
            if (pos >= src.length() || src.charAt(pos) != ':')
                throw new IllegalArgumentException("Expected ':' at " + pos);
            pos++;
            Object val = parseValue();
            m.put(key, val);
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ',') { pos++; continue; }
            if (pos < src.length() && src.charAt(pos) == '}') { pos++; break; }
            throw new IllegalArgumentException("Expected ',' or '}' at " + pos);
        }
        return m;
    }

    private List<Object> parseArrayValue() {
        List<Object> list = new ArrayList<>();
        pos++;
        skipWs();
        if (pos < src.length() && src.charAt(pos) == ']') { pos++; return list; }
        while (true) {
            list.add(parseValue());
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ',') { pos++; skipWs(); continue; }
            if (pos < src.length() && src.charAt(pos) == ']') { pos++; break; }
            throw new IllegalArgumentException("Expected ',' or ']' at " + pos);
        }
        return list;
    }

    private String parseString() {
        if (src.charAt(pos) != '"') throw new IllegalArgumentException("Expected string at " + pos);
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (pos >= src.length()) break;
                char e = src.charAt(pos++);
                switch (e) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (pos + 4 <= src.length()) {
                            sb.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                            pos += 4;
                        }
                    }
                    default -> sb.append(e);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Object parseNumber() {
        int start = pos;
        while (pos < src.length() && "+-0123456789.eE".indexOf(src.charAt(pos)) >= 0) pos++;
        String num = src.substring(start, pos);
        if (num.contains(".") || num.contains("e") || num.contains("E")) return Double.parseDouble(num);
        return Long.parseLong(num);
    }

    private Boolean parseBool() {
        if (src.startsWith("true", pos)) { pos += 4; return true; }
        if (src.startsWith("false", pos)) { pos += 5; return false; }
        return null;
    }

    private Object parseNull() {
        if (src.startsWith("null", pos)) { pos += 4; return null; }
        return null;
    }

    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }
}
