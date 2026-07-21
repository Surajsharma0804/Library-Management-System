package com.library.interfaces;

import java.util.Map;

/**
 * Contract for mapping entities to/from JSON-ready maps.
 */
public interface JsonMappable<T> {
    Map<String, Object> toMap(T entity);
    T fromMap(Map<String, Object> map);
}
