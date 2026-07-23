package com.library.persistence;

import java.util.List;
import java.util.Map;

/**
 * Storage backend abstraction. Repositories call readAll() / writeAll()
 * rather than touching the filesystem or database directly.
 */
public interface StorageBackend {
    /** Read all records from the named store. */
    List<Map<String, Object>> readAll(String storeName) throws StorageException;

    /** Replace all records in the named store atomically. */
    void writeAll(String storeName, List<Map<String, Object>> records) throws StorageException;

    /** Return the backend type identifier. */
    String type(); // "JSON" or "SQLITE"
}
