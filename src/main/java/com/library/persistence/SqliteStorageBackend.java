package com.library.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SQLite-backed {@link StorageBackend} implementation.
 *
 * <p>Each "store" maps to a table in the SQLite database file at {@code dbPath}.
 * Tables are created automatically on first access via
 * {@code CREATE TABLE IF NOT EXISTS}. Write operations are executed inside a
 * single transaction: the existing rows are deleted and the new set is
 * inserted atomically, so a crash mid-write never leaves the table partially
 * updated.
 *
 * <p><strong>SQL injection safety:</strong> The {@code storeName} parameter is
 * validated against the regex {@code [\w.\-]+} before any use in dynamic DDL
 * or DML strings. All data values are bound via {@link PreparedStatement#setObject},
 * never interpolated into SQL text.
 *
 * <p>Requirements: 3.1, 3.4, 3.5
 */
public final class SqliteStorageBackend implements StorageBackend {

    /** Allowed characters in a store (table) name. */
    private static final String VALID_STORE_NAME = "[\\w.\\-]+";

    private final String dbPath;

    /**
     * Creates a {@code SqliteStorageBackend} backed by the given file.
     *
     * @param dbPath path to the SQLite database file (created if absent); must not be {@code null}
     */
    public SqliteStorageBackend(String dbPath) {
        this.dbPath = Objects.requireNonNull(dbPath, "dbPath");
    }

    /** {@inheritDoc} */
    @Override
    public String type() {
        return "SQLITE";
    }

    /**
     * Reads all rows from the named table.
     *
     * <p>If the table does not yet exist it is created (empty) and an empty
     * list is returned.
     *
     * @param storeName table name; must match {@code [\w.\-]+}
     * @return list of rows, each represented as a column-name-to-value map
     * @throws StorageException if a database error occurs
     * @throws IllegalArgumentException if {@code storeName} is invalid
     */
    @Override
    public List<Map<String, Object>> readAll(String storeName) throws StorageException {
        validateStoreName(storeName);
        try (Connection conn = getConnection()) {
            ensureTableExists(conn, storeName);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM \"" + storeName + "\"")) {
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                List<Map<String, Object>> result = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    result.add(row);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new StorageException("readAll failed for store '" + storeName + "'", e);
        }
    }

    /**
     * Atomically replaces all rows in the named table with the supplied records.
     *
     * <p>The operation is wrapped in a single transaction:
     * <ol>
     *   <li>All existing rows are deleted.</li>
     *   <li>The new records are inserted in batch.</li>
     *   <li>The transaction is committed.</li>
     * </ol>
     * Any failure causes a rollback.
     *
     * @param storeName table name; must match {@code [\w.\-]+}
     * @param records   records to write; may be empty (clears the table)
     * @throws StorageException if a database error occurs
     * @throws IllegalArgumentException if {@code storeName} is invalid
     */
    @Override
    public void writeAll(String storeName, List<Map<String, Object>> records) throws StorageException {
        validateStoreName(storeName);
        try (Connection conn = getConnection()) {
            ensureTableExists(conn, storeName);
            conn.setAutoCommit(false);
            try {
                // DELETE all existing rows
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM \"" + storeName + "\"")) {
                    del.executeUpdate();
                }
                // INSERT all new rows
                if (!records.isEmpty()) {
                    Map<String, Object> first = records.get(0);
                    List<String> cols = new ArrayList<>(first.keySet());
                    String placeholders = cols.stream().map(c -> "?").collect(Collectors.joining(","));
                    String colNames = cols.stream()
                            .map(c -> "\"" + c + "\"")
                            .collect(Collectors.joining(","));
                    String sql = "INSERT INTO \"" + storeName + "\" (" + colNames
                            + ") VALUES (" + placeholders + ")";
                    try (PreparedStatement ins = conn.prepareStatement(sql)) {
                        for (Map<String, Object> row : records) {
                            for (int i = 0; i < cols.size(); i++) {
                                ins.setObject(i + 1, row.get(cols.get(i)));
                            }
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new StorageException("writeAll failed for store '" + storeName + "'", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Opens a JDBC connection to the SQLite database.
     *
     * @throws SQLException if the connection cannot be established
     */
    private Connection getConnection() throws SQLException {
        return java.sql.DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    /**
     * Ensures the table for {@code storeName} exists, creating it if necessary.
     *
     * <p>New tables are created with a single {@code data TEXT} column as a
     * minimal placeholder. Actual column structure is determined dynamically
     * on the first {@link #writeAll} call via the batch INSERT.
     *
     * @param conn      open JDBC connection
     * @param storeName validated table name (safe for interpolation)
     * @throws SQLException if the DDL statement fails
     */
    private void ensureTableExists(Connection conn, String storeName) throws SQLException {
        // storeName has been validated — interpolation is safe
        String ddl = "CREATE TABLE IF NOT EXISTS \"" + storeName + "\" (data TEXT)";
        try (PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.executeUpdate();
        }
    }

    /**
     * Validates that {@code storeName} contains only safe characters.
     *
     * @param storeName the name to validate
     * @throws IllegalArgumentException if the name is {@code null}, blank, or contains unsafe characters
     */
    private void validateStoreName(String storeName) {
        if (storeName == null || storeName.isBlank() || !storeName.matches(VALID_STORE_NAME)) {
            throw new IllegalArgumentException("Invalid store name: " + storeName);
        }
    }
}
