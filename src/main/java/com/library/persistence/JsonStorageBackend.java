package com.library.persistence;

import com.library.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON-file-backed storage backend.
 *
 * <p>Reads and writes JSON arrays from/to files on disk. Writes are
 * performed atomically: the new content is first written to a {@code .tmp}
 * sibling file and then renamed into place via
 * {@link StandardCopyOption#ATOMIC_MOVE}, so a crash mid-write never
 * leaves the store in a partially-written state.
 *
 * <p>The {@code storeName} argument passed to {@link #readAll} and
 * {@link #writeAll} is treated as a file path relative to the process
 * working directory (e.g. {@code "data/books.json"}), matching the
 * convention already used throughout {@code JsonRepository}.
 *
 * <p>Requirements: 3.1
 */
public class JsonStorageBackend implements StorageBackend {

    /**
     * Creates a new {@code JsonStorageBackend}.
     *
     * <p>No base-directory parameter is required because {@code storeName}
     * values already carry the full relative path (e.g.
     * {@code "data/books.json"}).
     */
    public JsonStorageBackend() {
        // no state to initialize
    }

    /**
     * Reads all records from the JSON file identified by {@code storeName}.
     *
     * @param storeName relative path to the JSON file (e.g. {@code "data/books.json"})
     * @return parsed records, or an empty list if the file does not exist or is blank
     * @throws StorageException if the file exists but cannot be read or parsed
     */
    @Override
    public List<Map<String, Object>> readAll(String storeName) throws StorageException {
        Path path = Path.of(storeName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            String content = Files.readString(path);
            if (content.isBlank()) {
                return new ArrayList<>();
            }
            return JsonUtils.parseArray(content);
        } catch (IOException e) {
            throw new StorageException("Failed to read store '" + storeName + "': " + e.getMessage(), e);
        } catch (Exception e) {
            throw new StorageException("Failed to parse store '" + storeName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Atomically replaces the contents of the JSON file identified by
     * {@code storeName} with the supplied records.
     *
     * <p>The sequence is:
     * <ol>
     *   <li>Ensure parent directories exist.</li>
     *   <li>Write JSON to {@code <storeName>.tmp}.</li>
     *   <li>Move the {@code .tmp} file over the target with
     *       {@link StandardCopyOption#ATOMIC_MOVE}.</li>
     * </ol>
     * If any step fails the {@code .tmp} file is deleted (best-effort) and
     * a {@link StorageException} is thrown.
     *
     * @param storeName relative path to the JSON file (e.g. {@code "data/books.json"})
     * @param records   the records to persist
     * @throws StorageException if the write or move fails
     */
    @Override
    public void writeAll(String storeName, List<Map<String, Object>> records) throws StorageException {
        Path targetPath = Path.of(storeName);
        Path tmpPath = Path.of(storeName + ".tmp");
        try {
            Files.createDirectories(targetPath.getParent());
            Files.writeString(tmpPath, JsonUtils.prettyPrint(records));
            Files.move(tmpPath, targetPath, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteTmpQuietly(tmpPath);
            throw new StorageException("Failed to write store '" + storeName + "': " + e.getMessage(), e);
        } catch (Exception e) {
            deleteTmpQuietly(tmpPath);
            throw new StorageException("Unexpected error writing store '" + storeName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns the backend type identifier.
     *
     * @return {@code "JSON"}
     */
    @Override
    public String type() {
        return "JSON";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void deleteTmpQuietly(Path tmpPath) {
        try {
            Files.deleteIfExists(tmpPath);
        } catch (IOException ignored) {
            // best-effort cleanup — suppress so the original exception propagates
        }
    }
}
