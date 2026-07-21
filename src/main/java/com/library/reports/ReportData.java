package com.library.reports;

import java.util.List;
import java.util.Objects;

/**
 * Immutable report payload: title, headers, rows, and summary.
 */
public record ReportData(String title, List<String> headers, List<List<String>> rows, String summary) {
    public ReportData {
        Objects.requireNonNull(title, "title");
        headers = headers == null ? List.of() : List.copyOf(headers);
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
