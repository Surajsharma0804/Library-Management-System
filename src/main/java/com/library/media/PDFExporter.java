package com.library.media;

import com.library.config.Constants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports tabular report data as a paginated, multi-page PDF document
 * using Apache PDFBox 3.x.
 *
 * <p>Each page contains:
 * <ul>
 *   <li>Header: library name, report title, and generation timestamp</li>
 *   <li>Table: column headers with a blue background, then rows with
 *       alternating white / light-grey fill</li>
 *   <li>Footer: "Page N of M" centred at the bottom</li>
 * </ul>
 *
 * <p>Output files are saved under {@code exports/reports/{reportName}_{yyyyMMdd_HHmmss}.pdf}.
 *
 * <p>Requirements: 12.6, 19.1
 */
public final class PDFExporter {

    /**
     * Immutable descriptor for a single report.
     *
     * @param reportName the human-readable name used in the file name and header
     * @param headers    column header labels
     * @param rows       data rows; each element must match the {@code headers} length
     */
    public record ReportData(String reportName, String[] headers, List<Object[]> rows) {}

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final float MARGIN        = 40f;
    private static final float ROW_HEIGHT    = 20f;
    private static final float HEADER_HEIGHT = 24f;
    private static final float PAGE_HEADER_Y_OFFSET = 60f; // space for page header

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color COLOR_HEADER_BG  = new Color(0x2c, 0x3e, 0x50);
    private static final Color COLOR_ROW_ALT    = new Color(0xf5, 0xf5, 0xf5);
    private static final Color COLOR_TEXT_DARK  = new Color(0x22, 0x22, 0x22);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Color COLOR_ACCENT     = new Color(0x34, 0x98, 0xdb);

    /**
     * Renders {@code data} as a paginated A4 PDF and saves it.
     *
     * @param data the report content; must not be {@code null}
     * @return the {@link Path} of the saved PDF file
     * @throws IOException if the output directory or file cannot be written
     */
    public Path export(ReportData data) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeName  = data.reportName().replaceAll("[^A-Za-z0-9_\\-]", "_");

        Path reportsDir = Path.of(Constants.EXPORT_DIR, "reports");
        Files.createDirectories(reportsDir);
        Path outputPath = reportsDir.resolve(safeName + "_" + timestamp + ".pdf");

        PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        try (PDDocument doc = new PDDocument()) {
            // ── Pre-calculate pagination ──────────────────────────────────────
            PDRectangle pageSize    = PDRectangle.A4;
            float pageWidth         = pageSize.getWidth();
            float pageHeight        = pageSize.getHeight();
            float usableWidth       = pageWidth - 2 * MARGIN;
            float tableTop          = pageHeight - MARGIN - PAGE_HEADER_Y_OFFSET;
            float tableBottom       = MARGIN + 20f; // room for footer

            float usableHeight      = tableTop - tableBottom - HEADER_HEIGHT;
            int   rowsPerPage       = Math.max(1, (int) (usableHeight / ROW_HEIGHT));

            List<Object[]> rows     = data.rows() != null ? data.rows() : List.of();
            String[]       headers  = data.headers() != null ? data.headers() : new String[0];
            int            colCount = headers.length;

            // Column width: equal split
            float colWidth = colCount > 0 ? usableWidth / colCount : usableWidth;

            // Split rows into pages
            List<List<Object[]>> pages = new ArrayList<>();
            for (int i = 0; i < rows.size() || pages.isEmpty(); i += rowsPerPage) {
                pages.add(rows.subList(i, Math.min(i + rowsPerPage, rows.size())));
            }
            int totalPages = pages.size();

            String libName   = Constants.LIBRARY_NAME;
            String generated = "Generated: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));

            for (int pageIdx = 0; pageIdx < totalPages; pageIdx++) {
                PDPage pdfPage = new PDPage(pageSize);
                doc.addPage(pdfPage);

                try (PDPageContentStream cs = new PDPageContentStream(doc, pdfPage)) {
                    // ── Page header ───────────────────────────────────────────
                    float headerY = pageHeight - MARGIN - 16f;

                    // Library name
                    cs.beginText();
                    cs.setFont(fontBold, 14);
                    cs.setNonStrokingColor(COLOR_TEXT_DARK);
                    cs.newLineAtOffset(MARGIN, headerY);
                    cs.showText(libName);
                    cs.endText();

                    // Report title centred
                    float titleWidth = fontBold.getStringWidth(data.reportName()) / 1000f * 12f;
                    cs.beginText();
                    cs.setFont(fontBold, 12);
                    cs.setNonStrokingColor(COLOR_ACCENT);
                    cs.newLineAtOffset((pageWidth - titleWidth) / 2f, headerY);
                    cs.showText(data.reportName());
                    cs.endText();

                    // Timestamp right-aligned
                    float tsWidth = fontRegular.getStringWidth(generated) / 1000f * 9f;
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(COLOR_TEXT_DARK);
                    cs.newLineAtOffset(pageWidth - MARGIN - tsWidth, headerY);
                    cs.showText(generated);
                    cs.endText();

                    // Accent separator line
                    cs.setNonStrokingColor(COLOR_ACCENT);
                    cs.addRect(MARGIN, headerY - 6f, usableWidth, 2f);
                    cs.fill();

                    // ── Column headers ────────────────────────────────────────
                    float hY = tableTop;

                    // Header background
                    cs.setNonStrokingColor(COLOR_HEADER_BG);
                    cs.addRect(MARGIN, hY - HEADER_HEIGHT, usableWidth, HEADER_HEIGHT);
                    cs.fill();

                    cs.setFont(fontBold, 10);
                    cs.setNonStrokingColor(COLOR_TEXT_LIGHT);
                    for (int c = 0; c < colCount; c++) {
                        String text = truncate(headers[c], colWidth, fontBold, 10f);
                        cs.beginText();
                        cs.newLineAtOffset(MARGIN + c * colWidth + 4f, hY - HEADER_HEIGHT + 7f);
                        cs.showText(text);
                        cs.endText();
                    }

                    // ── Data rows ─────────────────────────────────────────────
                    List<Object[]> pageRows = pages.get(pageIdx);
                    float rowY = hY - HEADER_HEIGHT;

                    for (int r = 0; r < pageRows.size(); r++) {
                        rowY -= ROW_HEIGHT;
                        Object[] row = pageRows.get(r);

                        // Alternating row background
                        if (r % 2 == 1) {
                            cs.setNonStrokingColor(COLOR_ROW_ALT);
                            cs.addRect(MARGIN, rowY, usableWidth, ROW_HEIGHT);
                            cs.fill();
                        }

                        cs.setFont(fontRegular, 9);
                        cs.setNonStrokingColor(COLOR_TEXT_DARK);
                        for (int c = 0; c < colCount; c++) {
                            String cellText = (row != null && c < row.length && row[c] != null)
                                    ? String.valueOf(row[c]) : "";
                            cellText = truncate(cellText, colWidth, fontRegular, 9f);
                            cs.beginText();
                            cs.newLineAtOffset(MARGIN + c * colWidth + 4f, rowY + 5f);
                            cs.showText(cellText);
                            cs.endText();
                        }

                        // Light horizontal divider
                        cs.setNonStrokingColor(new Color(0xdd, 0xdd, 0xdd));
                        cs.addRect(MARGIN, rowY, usableWidth, 0.5f);
                        cs.fill();
                    }

                    // ── Footer ────────────────────────────────────────────────
                    String footerText = "Page " + (pageIdx + 1) + " of " + totalPages;
                    float footerWidth = fontRegular.getStringWidth(footerText) / 1000f * 9f;
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.setNonStrokingColor(new Color(0x88, 0x88, 0x88));
                    cs.newLineAtOffset((pageWidth - footerWidth) / 2f, MARGIN - 10f);
                    cs.showText(footerText);
                    cs.endText();
                }
            }

            doc.save(outputPath.toFile());
        }

        return outputPath;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Truncates {@code text} so that its rendered width fits within {@code maxWidth}
     * pixels for the given font and size. Appends "…" when truncated.
     */
    private static String truncate(String text, float maxWidth, PDType1Font font, float fontSize) {
        if (text == null || text.isEmpty()) return "";
        try {
            float textWidth = font.getStringWidth(text) / 1000f * fontSize;
            if (textWidth <= maxWidth - 8f) return text;

            // Binary-search for the longest prefix that fits
            String ellipsis = "...";
            float ellipsisWidth = font.getStringWidth(ellipsis) / 1000f * fontSize;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                String candidate = sb.toString() + text.charAt(i);
                float w = font.getStringWidth(candidate) / 1000f * fontSize + ellipsisWidth;
                if (w > maxWidth - 8f) break;
                sb.append(text.charAt(i));
            }
            return sb + ellipsis;
        } catch (IOException e) {
            // Fall back to raw substring if width calculation fails
            int max = Math.max(3, (int) (maxWidth / (fontSize * 0.6)));
            return text.length() > max ? text.substring(0, max - 3) + "..." : text;
        }
    }
}
