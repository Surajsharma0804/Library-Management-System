package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Executive Analytics & Insights Panel — 6 stat cards, bar + pie charts,
 * and top-10 most-borrowed books table.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class AnalyticsPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    // Top stat cards row
    private JPanel statsRow;

    // Charts row
    private JPanel chartsRow;

    // Bottom table
    private JTable topBooksTable;
    private DefaultTableModel topBooksModel;

    public AnalyticsPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(AppTheme.heading("Executive Analytics & Insights"));
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(AppTheme.label2("Visual data breakdowns, category distributions, and circulation trends"));

        // ── 6 stat cards ──────────────────────────────────────────────────────
        statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 110));

        // ── Charts row ────────────────────────────────────────────────────────
        chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setPreferredSize(new Dimension(0, 280));

        // ── Top-10 most-borrowed table ─────────────────────────────────────
        topBooksModel = new DefaultTableModel(
                new String[]{"#", "Book Title", "Author", "Category", "Borrow Count"}, 0);
        topBooksTable = new JTable(topBooksModel) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        AppTheme.styleTable(topBooksTable);
        topBooksTable.getColumnModel().getColumn(1).setPreferredWidth(260);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        JLabel topLabel = AppTheme.heading("Top 10 Most Borrowed Books");
        topLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        bottomPanel.add(topLabel, BorderLayout.NORTH);
        bottomPanel.add(AppTheme.scroll(topBooksTable), BorderLayout.CENTER);

        // ── Main scrollable content ───────────────────────────────────────────
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(statsRow);
        inner.add(Box.createVerticalStrut(16));
        inner.add(chartsRow);
        inner.add(Box.createVerticalStrut(16));
        inner.add(bottomPanel);

        JScrollPane sp = new JScrollPane(inner);
        sp.setOpaque(false); sp.getViewport().setOpaque(false);
        sp.setBorder(null); sp.getVerticalScrollBar().setUnitIncrement(16);

        add(hdr, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());

        // Check ANALYTICS_VIEW permission
        try {
            facade.rbac().require(session, Permissions.ANALYTICS_VIEW);
        } catch (Exception ex) {
            statsRow.removeAll();
            statsRow.add(AppTheme.metricCard("Access Denied", "No ANALYTICS_VIEW", AppTheme.RED));
            revalidate(); repaint();
            return;
        }

        new SwingWorker<Void, Void>() {
            // data fields filled in doInBackground
            long totalBooks, available, borrowed, overdue, activeStudents, outstandingFineAmt;
            Map<String, Long> monthlyData, categoryData;
            List<Book> topBooks;

            @Override
            protected Void doInBackground() {
                totalBooks       = facade.analytics().totalBooks();
                available        = facade.books().findAll().stream()
                                       .filter(b -> b.getAvailableQuantity() > 0).count();
                borrowed         = facade.analytics().totalActiveBorrows();
                overdue          = facade.analytics().totalOverdueBorrows();
                activeStudents   = facade.analytics().totalStudents();
                outstandingFineAmt = facade.analytics().totalPendingFineAmountPaise();
                monthlyData      = facade.analytics().monthlyBorrowCounts(Year.now().getValue());
                categoryData     = facade.analytics().booksByCategory();
                topBooks         = facade.analytics().mostBorrowedBooks(10);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // propagate exceptions
                    updateUI();
                } catch (Exception ex) {
                    statsRow.removeAll();
                    statsRow.add(AppTheme.metricCard("Error", ex.getMessage(), AppTheme.RED));
                    revalidate(); repaint();
                }
            }

            private void updateUI() {
                // ── 6 stat cards ──────────────────────────────────────────
                statsRow.removeAll();
                statsRow.add(AppTheme.metricCard("Total Books",
                        String.valueOf(totalBooks), "In catalog", AppTheme.ACCENT));
                statsRow.add(AppTheme.metricCard("Available",
                        String.valueOf(available), "Ready for checkout", AppTheme.GREEN));
                statsRow.add(AppTheme.metricCard("Borrowed",
                        String.valueOf(borrowed), "Currently out", AppTheme.AMBER));
                statsRow.add(AppTheme.metricCard("Overdue",
                        String.valueOf(overdue), "Needs attention", AppTheme.RED));
                statsRow.add(AppTheme.metricCard("Active Students",
                        String.valueOf(activeStudents), "Registered members", AppTheme.VIOLET));
                statsRow.add(AppTheme.metricCard("Outstanding Fines",
                        String.format("₹%.0f", outstandingFineAmt / 100.0),
                        "Pending collection", AppTheme.ROSE));

                // ── Charts ────────────────────────────────────────────────
                chartsRow.removeAll();

                // Bar chart — Monthly Borrows
                String[] months = monthlyData.keySet().toArray(new String[0]);
                long[] monthVals = monthlyData.values().stream().mapToLong(Long::longValue).toArray();
                ChartPanel barChart = ChartPanel.barChart(
                        "Monthly Borrows (" + Year.now().getValue() + ")", months, monthVals);
                barChart.setBackground(AppTheme.bgCard());
                barChart.setPreferredSize(new Dimension(0, 260));

                // Pie chart — Books by Category
                String[] cats = categoryData.keySet().stream().limit(8).toArray(String[]::new);
                long[] catVals = categoryData.values().stream()
                        .limit(8).mapToLong(Long::longValue).toArray();
                ChartPanel pieChart = ChartPanel.pieChart("Books by Category", cats, catVals);
                pieChart.setBackground(AppTheme.bgCard());
                pieChart.setPreferredSize(new Dimension(0, 260));

                chartsRow.add(barChart);
                chartsRow.add(pieChart);

                // ── Top-10 table ──────────────────────────────────────────
                topBooksModel.setRowCount(0);
                int rank = 1;
                for (Book b : topBooks) {
                    topBooksModel.addRow(new Object[]{
                            rank++,
                            b.getTitle(),
                            b.getAuthor(),
                            b.getCategory() != null ? b.getCategory() : "-",
                            "-"   // borrow count not directly on Book; shown via rank
                    });
                }
                AppTheme.styleTable(topBooksTable);

                revalidate(); repaint();
            }
        }.execute();
    }
}
