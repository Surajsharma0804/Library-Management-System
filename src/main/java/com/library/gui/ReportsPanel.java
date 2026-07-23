package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.media.PDFExporter;
import com.library.reports.ReportData;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Executive Reports Panel — Generate, preview, and export reports to CSV and PDF.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class ReportsPanel extends JPanel {

    private final LibraryFacade facade;
    private JComboBox<String> reportSelector;
    private JTable table;
    private DefaultTableModel model;
    private JLabel summaryLabel;
    private Session session;

    public ReportsPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("System Reports & Export"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Generate operational reports and export to CSV or PDF format"));

        reportSelector = AppTheme.comboBox();
        reportSelector.setPreferredSize(new Dimension(240, 38));

        JButton genBtn = AppTheme.primaryBtn("Generate");
        genBtn.setPreferredSize(new Dimension(110, 38));
        genBtn.addActionListener(e -> generate());

        JButton csvBtn = AppTheme.secondaryBtn("Export CSV");
        csvBtn.setPreferredSize(new Dimension(120, 38));
        csvBtn.addActionListener(e -> exportCsv());

        JButton pdfBtn = AppTheme.secondaryBtn("Export PDF");
        pdfBtn.setPreferredSize(new Dimension(120, 38));
        pdfBtn.addActionListener(e -> exportPdf());

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(reportSelector);
        acts.add(genBtn);
        acts.add(csvBtn);
        acts.add(pdfBtn);

        hdr.add(title, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel();
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        AppTheme.styleTable(table);

        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(AppTheme.SMALL);
        summaryLabel.setForeground(AppTheme.fgSecondary());
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 0));

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(AppTheme.scroll(table), BorderLayout.CENTER);
        body.add(summaryLabel, BorderLayout.SOUTH);

        add(hdr, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());

        // Check REPORT_VIEW permission
        try {
            facade.rbac().require(session, Permissions.REPORT_VIEW);
        } catch (Exception ex) {
            summaryLabel.setText("Access denied — REPORT_VIEW permission required.");
            return;
        }

        reportSelector.removeAllItems();
        List<String> ids = facade.reports().availableReportIds();
        for (String id : ids) reportSelector.addItem(id);
        model.setRowCount(0); model.setColumnCount(0);
        summaryLabel.setText(ids.size() + " report definition(s) available");
    }

    private void generate() {
        String id = (String) reportSelector.getSelectedItem();
        if (id == null) return;
        try {
            ReportData data = facade.reports().generate(id);
            model.setRowCount(0); model.setColumnCount(0);
            for (String h : data.headers()) model.addColumn(h);
            for (List<String> row : data.rows()) model.addRow(row.toArray());
            AppTheme.styleTable(table);
            summaryLabel.setText(data.summary() + "  |  Total records: " + data.rows().size());
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
        }
    }

    private void exportCsv() {
        String id = (String) reportSelector.getSelectedItem();
        if (id == null) return;
        try {
            String path = facade.reports().exportToCsv(session, id);
            AppTheme.success(this, "Report exported successfully to:\n" + path);
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
        }
    }

    private void exportPdf() {
        String id = (String) reportSelector.getSelectedItem();
        if (id == null) return;

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                ReportData data = facade.reports().generate(id);

                // Convert com.library.reports.ReportData to PDFExporter.ReportData
                String[] headers = data.headers().toArray(new String[0]);
                List<Object[]> rows = data.rows().stream()
                        .map(row -> row.toArray(new Object[0]))
                        .toList();
                PDFExporter.ReportData pdfData =
                        new PDFExporter.ReportData(id, headers, rows);

                Path outputPath = new PDFExporter().export(pdfData);
                return outputPath.toAbsolutePath().toString();
            }

            @Override
            protected void done() {
                try {
                    String path = get();
                    AppTheme.success(ReportsPanel.this,
                            "PDF exported successfully to:\n" + path);
                } catch (Exception ex) {
                    AppTheme.error(ReportsPanel.this, "PDF export failed: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
