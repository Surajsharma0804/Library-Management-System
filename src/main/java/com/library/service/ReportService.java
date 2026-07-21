package com.library.service;

import com.library.service.AuditService;
import com.library.config.Constants;
import com.library.reports.ReportData;
import com.library.reports.ReportStrategy;
import com.library.security.Session;
import com.library.util.DateUtils;
import com.library.util.FileUtils;
import com.library.util.StringUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade over report generation and export. Holds a registry of
 * {@link ReportStrategy} instances keyed by id, dispatches generation
 * requests, renders reports to the console, and exports to CSV.
 */
public final class ReportService {

    private final Map<String, ReportStrategy> registry = new LinkedHashMap<>();
    private final AuditService auditService;

    public ReportService(AuditService auditService) {
        this.auditService = auditService;
    }

    public ReportService register(ReportStrategy strategy) {
        registry.put(strategy.id(), Objects.requireNonNull(strategy));
        return this;
    }

    public ReportData generate(String reportId) {
        ReportStrategy strategy = registry.get(reportId);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown report: " + reportId);
        }
        return strategy.generate();
    }

    public List<String> availableReportIds() {
        return List.copyOf(registry.keySet());
    }

    public String renderToText(ReportData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(data.title()).append(" ===\n");
        sb.append("Generated: ").append(DateUtils.formatDateTime(DateUtils.now())).append("\n\n");
        if (!data.headers().isEmpty()) {
            int[] widths = computeWidths(data.headers(), data.rows());
            appendRow(sb, data.headers(), widths);
            appendSeparator(sb, widths);
            for (List<String> row : data.rows()) {
                appendRow(sb, row, widths);
            }
        }
        sb.append("\n").append(data.summary()).append("\n");
        return sb.toString();
    }

    public String exportToCsv(Session session, String reportId) {
        ReportData data = generate(reportId);
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", data.headers())).append(System.lineSeparator());
        for (List<String> row : data.rows()) {
            sb.append(row.stream().map(this::csvEscape).reduce((a, b) -> a + "," + b).orElse(""))
              .append(System.lineSeparator());
        }
        String filename = reportId + "-" + DateUtils.format(DateUtils.today()) + ".csv";
        Path file = Path.of(Constants.EXPORT_DIR).resolve(filename);
        FileUtils.write(file, sb.toString());
        auditService.record(session, "REPORT_EXPORT", "Report", reportId,
                "Exported " + reportId + " to " + file);
        return file.toString();
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private int[] computeWidths(List<String> headers, List<List<String>> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(row.size(), widths.length); i++) {
                widths[i] = Math.max(widths[i], Math.min(row.get(i) == null ? 0 : row.get(i).length(), 40));
            }
        }
        return widths;
    }

    private void appendRow(StringBuilder sb, List<String> row, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            String cell = i < row.size() && row.get(i) != null ? row.get(i) : "";
            sb.append(StringUtils.pad(cell, widths[i])).append(" | ");
        }
        sb.append(System.lineSeparator());
    }

    private void appendSeparator(StringBuilder sb, int[] widths) {
        for (int width : widths) {
            sb.append("-".repeat(width)).append("-+-");
        }
        sb.append(System.lineSeparator());
    }
}
