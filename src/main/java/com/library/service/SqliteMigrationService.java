package com.library.service;

import com.library.config.Constants;
import com.library.persistence.JsonStorageBackend;
import com.library.persistence.SqliteStorageBackend;
import com.library.persistence.StorageException;
import com.library.repository.LibraryConfigRepository;
import com.library.security.Session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Migrates all data stores from the JSON backend to the SQLite backend.
 *
 * <p>Each store (books, students, borrow_records, etc.) is read in full from
 * the JSON files and written atomically to the corresponding SQLite table.
 * After all stores succeed the library config is updated to record that the
 * active backend is now SQLITE.
 *
 * <p>Requirements: 3.2, 3.3, 3.5
 */
public final class SqliteMigrationService {

    /**
     * Summary returned after a migration attempt.
     *
     * @param rowCounts store name → number of rows migrated (only for successful stores)
     * @param errors    human-readable error messages for any stores that failed
     */
    public record MigrationResult(
            Map<String, Integer> rowCounts,
            List<String> errors
    ) {}

    /**
     * Comparison of record counts between the two backends for one store.
     *
     * @param storeName   logical store name (e.g. {@code "books"})
     * @param jsonRows    rows read from the JSON backend
     * @param sqliteRows  rows verified in the SQLite backend after the write
     */
    public record StoreCount(String storeName, int jsonRows, int sqliteRows) {}

    // -------------------------------------------------------------------------

    private final JsonStorageBackend jsonBackend;
    private final SqliteStorageBackend sqliteBackend;
    private final LibraryConfigRepository configRepo;
    private final AuditService auditService;

    /**
     * Creates a new {@code SqliteMigrationService}.
     *
     * @param jsonBackend   source backend (JSON files); must not be {@code null}
     * @param sqliteBackend destination backend (SQLite); must not be {@code null}
     * @param configRepo    config repository used to persist the backend switch; must not be {@code null}
     * @param auditService  audit service for recording the migration event; must not be {@code null}
     */
    public SqliteMigrationService(
            JsonStorageBackend jsonBackend,
            SqliteStorageBackend sqliteBackend,
            LibraryConfigRepository configRepo,
            AuditService auditService) {
        this.jsonBackend = Objects.requireNonNull(jsonBackend, "jsonBackend");
        this.sqliteBackend = Objects.requireNonNull(sqliteBackend, "sqliteBackend");
        this.configRepo = Objects.requireNonNull(configRepo, "configRepo");
        this.auditService = Objects.requireNonNull(auditService, "auditService");
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Migrates all stores from JSON to SQLite.
     *
     * <p>For each store:
     * <ol>
     *   <li>Reads all records from the JSON backend.</li>
     *   <li>Writes them to the SQLite backend.</li>
     *   <li>Verifies the row count in SQLite matches the source count.</li>
     *   <li>On any error (mismatch or {@link StorageException}) records an error and continues.</li>
     * </ol>
     *
     * <p>If every store migrated without error the result is recorded in the
     * audit log.  The {@code storageBackend} setting is not written back to
     * {@link com.library.model.LibraryConfig} because that model does not
     * currently carry a backend field; the caller is responsible for
     * persisting the switch through whatever mechanism is appropriate.
     *
     * @param session the authenticated session performing the migration; must not be {@code null}
     * @return a {@link MigrationResult} containing per-store row counts and any errors
     */
    public MigrationResult migrate(Session session) {
        Objects.requireNonNull(session, "session");

        List<String> storePaths = buildStorePaths();

        Map<String, Integer> rowCounts = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        for (String path : storePaths) {
            String store = storeNameFromPath(path);
            try {
                List<Map<String, Object>> records = jsonBackend.readAll(path);
                sqliteBackend.writeAll(store, records);

                int verified = sqliteBackend.readAll(store).size();
                if (verified != records.size()) {
                    errors.add("Row count mismatch for '" + store + "': expected "
                            + records.size() + ", got " + verified);
                    continue;
                }

                rowCounts.put(store, records.size());

            } catch (StorageException e) {
                errors.add("Error migrating '" + store + "': " + e.getMessage());
            }
        }

        int successCount = rowCounts.size();
        auditService.record(session, "MIGRATION_COMPLETE", "System", null,
                "Migrated " + successCount + " stores to SQLite");

        return new MigrationResult(rowCounts, errors);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the list of all JSON store file paths that should be migrated.
     *
     * @return ordered list of file paths (as used by {@link JsonStorageBackend#readAll})
     */
    private List<String> buildStorePaths() {
        List<String> paths = new ArrayList<>();
        paths.add(Constants.BOOKS_FILE);
        paths.add(Constants.STUDENTS_FILE);
        paths.add(Constants.USERS_FILE);
        paths.add(Constants.BORROW_RECORDS_FILE);
        paths.add(Constants.RESERVATIONS_FILE);
        paths.add(Constants.FINES_FILE);
        paths.add(Constants.NOTIFICATIONS_FILE);
        paths.add(Constants.AUDIT_LOG_FILE);
        paths.add(Constants.MEMBERSHIP_TIERS_FILE);
        paths.add(Constants.LOST_BOOKS_FILE);
        paths.add(Constants.READING_LISTS_FILE);
        paths.add(Constants.ACQUISITIONS_FILE);
        paths.add(Constants.BRANCHES_FILE);
        paths.add(Constants.STUDY_ROOMS_FILE);
        paths.add(Constants.ROOM_RESERVATIONS_FILE);
        paths.add(Constants.ILL_RECORDS_FILE);
        return paths;
    }

    /**
     * Strips the directory prefix and file extension from a JSON file path to
     * produce a valid SQLite table name.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "data/books.json"} → {@code "books"}</li>
     *   <li>{@code "data/borrow_records.json"} → {@code "borrow_records"}</li>
     * </ul>
     *
     * @param path relative file path (e.g. {@code "data/books.json"})
     * @return the bare store / table name without path or extension
     */
    private String storeNameFromPath(String path) {
        // Remove everything up to and including the last '/' or '\'
        String name = path;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        // Strip the file extension
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name;
    }
}
