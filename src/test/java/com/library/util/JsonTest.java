package com.library.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON codec tests")
class JsonTest {

    @Test
    @DisplayName("Parses a simple object")
    void parsesObject() {
        Object result = JsonUtils.parse("{\"name\":\"Alice\",\"age\":30}");
        Map<String, Object> map = JsonUtils.asObject(result);
        assertEquals("Alice", map.get("name"));
        assertEquals(30L, map.get("age"));
    }

    @Test
    @DisplayName("Parses nested objects and arrays")
    void parsesNested() {
        Object result = JsonUtils.parse("{\"books\":[{\"title\":\"A\"},{\"title\":\"B\"}],\"count\":2}");
        Map<String, Object> map = JsonUtils.asObject(result);
        List<Object> books = JsonUtils.getArray(map, "books");
        assertEquals(2, books.size());
        assertEquals("A", JsonUtils.asObject(books.get(0)).get("title"));
        assertEquals(2L, map.get("count"));
    }

    @Test
    @DisplayName("Parses booleans and null")
    void parsesBooleansAndNull() {
        Object result = JsonUtils.parse("{\"active\":true,\"deleted\":false,\"note\":null}");
        Map<String, Object> map = JsonUtils.asObject(result);
        assertEquals(Boolean.TRUE, map.get("active"));
        assertEquals(Boolean.FALSE, map.get("deleted"));
        assertNull(map.get("note"));
    }

    @Test
    @DisplayName("Parses doubles and longs")
    void parsesNumbers() {
        Object result = JsonUtils.parse("{\"price\":19.99,\"count\":42}");
        Map<String, Object> map = JsonUtils.asObject(result);
        assertEquals(19.99, map.get("price"));
        assertEquals(42L, map.get("count"));
    }

    @Test
    @DisplayName("Serializes a map to JSON")
    void serializesMap() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("name", "Bob");
        map.put("age", 25);
        map.put("tags", List.of("a", "b"));
        String json = JsonUtils.stringify(map);
        Object parsed = JsonUtils.parse(json);
        Map<String, Object> back = JsonUtils.asObject(parsed);
        assertEquals("Bob", back.get("name"));
        assertEquals(25L, back.get("age"));
    }

    @Test
    @DisplayName("Handles escape sequences in strings")
    void handlesEscapes() {
        Object result = JsonUtils.parse("{\"text\":\"Hello\\nWorld\\t!\"}");
        Map<String, Object> map = JsonUtils.asObject(result);
        assertEquals("Hello\nWorld\t!", map.get("text"));
    }

    @Test
    @DisplayName("Serializes strings with special characters")
    void serializesSpecialChars() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("value", "Quote\"Newline\nTab\t");
        String json = JsonUtils.stringify(map);
        Object parsed = JsonUtils.parse(json);
        assertEquals("Quote\"Newline\nTab\t", JsonUtils.asObject(parsed).get("value"));
    }

    @Test
    @DisplayName("Throws on invalid JSON")
    void throwsOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> JsonUtils.parse("{invalid}"));
        assertThrows(IllegalArgumentException.class, () -> JsonUtils.parse(""));
        assertThrows(IllegalArgumentException.class, () -> JsonUtils.parse("[1,2,"));
    }

    @Test
    @DisplayName("Handles empty objects and arrays")
    void handlesEmpty() {
        assertEquals(0, JsonUtils.asObject(JsonUtils.parse("{}")).size());
        assertEquals(0, JsonUtils.asArray(JsonUtils.parse("[]")).size());
    }

    @Test
    @DisplayName("Pretty prints JSON")
    void prettyPrints() {
        String json = "{\"a\":1,\"b\":[1,2]}";
        String pretty = JsonUtils.prettyPrint(json);
        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("  "));
    }

    @Test
    @DisplayName("requireInt throws on missing field")
    void requireIntThrows() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("name", "test");
        assertThrows(IllegalArgumentException.class, () -> JsonUtils.requireInt(map, "missing"));
    }

    @Test
    @DisplayName("getString returns null for missing field")
    void getStringReturnsNull() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        assertNull(JsonUtils.getString(map, "missing"));
    }
}
