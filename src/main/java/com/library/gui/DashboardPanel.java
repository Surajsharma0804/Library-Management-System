package com.library.gui;

import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.Year;
import java.util.Map;

/**
 * Executive Dashboard Panel — Role-specific dashboards for Admin, Librarian, and Student.
 * Admin: 7 stat cards + bar chart. Librarian/Student: classic metrics + timeline.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class DashboardPanel extends JPanel {

    private final LibraryFacade facade;

    public DashboardPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
    }

    public void refresh(Session session) {
        setBackground(AppTheme.bg());
        removeAll();

        if (session != null && session.role() == UserRole.ADMIN) {
            buildAdminDashboard(session);
        } else if (session != null && session.role() == UserRole.LIBRARIAN) {
            buildLibrarianDashboard(session);
        } else {
            buildDefaultDashboard(session);
        }

        revalidate(); repaint();
    }

    // ── Admin Dashboard ───────────────────────────────────────────────────────

    private void buildAdminDashboard(Session session) {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Administration Dashboard");
        t.setFont(AppTheme.H1); t.setForeground(AppTheme.fg());
        JLabel s = new JLabel("System-wide metrics and circulation overview");
        s.setFont(AppTheme.SMALL); s.setForeground(AppTheme.fgSecondary());
        titleBlock.add(t);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(s);
        header.add(titleBlock, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // 7 stat cards placeholder row
        JPanel statsRow = new JPanel(new GridLayout(1, 7, 10, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 110));

        // Chart area
        JPanel chartArea = new JPanel(new BorderLayout());
        chartArea.setOpaque(false);

        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);
        mainContent.add(statsRow, BorderLayout.NORTH);
        mainContent.add(chartArea, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // Load data in SwingWorker
        new SwingWorker<int[], Void>() {
            int totalBooks, availBooks, activeBorrows, overdueBorrows,
                    pendingFines, totalLibrarians, pendingReservations;
            Map<String, Long> monthlyData;

            @Override
            protected int[] doInBackground() {
                var stats = facade.dashboard().adminDashboard(session);
                totalBooks         = stats.getTotalBooks();
                availBooks         = stats.getAvailableBooks();
                activeBorrows      = stats.getBorrowedBooks();
                overdueBorrows     = stats.getOverdueBooks();
                pendingFines       = stats.getPendingFines();
                totalLibrarians    = facade.users().findAll().size();
                pendingReservations = stats.getPendingReservations();
                monthlyData        = facade.analytics()
                        .monthlyBorrowCounts(Year.now().getValue());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    statsRow.add(AppTheme.metricCard("Error", ex.getMessage(), AppTheme.RED));
                    revalidate(); repaint();
                    return;
                }

                statsRow.removeAll();
                statsRow.add(AppTheme.metricCard("Total Books",
                        String.valueOf(totalBooks), "In catalog", AppTheme.ACCENT));
                statsRow.add(AppTheme.metricCard("Total Students",
                        String.valueOf(facade.analytics().totalStudents()),
                        "Registered", AppTheme.GREEN));
                statsRow.add(AppTheme.metricCard("Active Borrows",
                        String.valueOf(activeBorrows), "Checked out", AppTheme.AMBER));
                statsRow.add(AppTheme.metricCard("Overdue Borrows",
                        String.valueOf(overdueBorrows), "Action required", AppTheme.RED));
                statsRow.add(AppTheme.metricCard("Pending Fines",
                        String.valueOf(pendingFines), "Awaiting payment", AppTheme.ROSE));
                statsRow.add(AppTheme.metricCard("Total Librarians",
                        String.valueOf(totalLibrarians), "Staff accounts", AppTheme.VIOLET));
                statsRow.add(AppTheme.metricCard("Pending Reservations",
                        String.valueOf(pendingReservations), "Queue", AppTheme.TEAL));

                // Bar chart
                chartArea.removeAll();
                String[] months = monthlyData.keySet().toArray(new String[0]);
                long[] vals     = monthlyData.values().stream().mapToLong(Long::longValue).toArray();
                ChartPanel bar  = ChartPanel.barChart(
                        "Monthly Circulation (" + Year.now().getValue() + ")", months, vals);
                bar.setBackground(AppTheme.bgCard());
                chartArea.add(bar, BorderLayout.CENTER);

                revalidate(); repaint();
            }
        }.execute();
    }

    // ── Librarian Dashboard ───────────────────────────────────────────────────

    private void buildLibrarianDashboard(Session session) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Librarian Dashboard");
        t.setFont(AppTheme.H1); t.setForeground(AppTheme.fg());
        JLabel s = new JLabel("Today's circulation activity and pending operations");
        s.setFont(AppTheme.SMALL); s.setForeground(AppTheme.fgSecondary());
        titleBlock.add(t);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(s);
        header.add(titleBlock, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 5, 10, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 110));

        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);
        mainContent.add(statsRow, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        new SwingWorker<Void, Void>() {
            int issuedToday, returnedToday, overdueCount, pendingReservations;
            long finesCollectedTodayPaise;

            @Override
            protected Void doInBackground() {
                try {
                    var stats = facade.dashboard().librarianDashboard(session);
                    overdueCount = stats.getOverdueBooks();
                    pendingReservations = stats.getPendingReservations();

                    java.time.LocalDate today = java.time.LocalDate.now();
                    issuedToday = (int) facade.borrows().findAllActive().stream()
                            .filter(b -> b.getIssueDate() != null && b.getIssueDate().equals(today))
                            .count();
                    returnedToday = (int) facade.borrowRepo().findAll().stream()
                            .filter(b -> b.getReturnDate() != null && b.getReturnDate().equals(today))
                            .count();
                    // Count paid fines today via all pending-check approach
                    finesCollectedTodayPaise = 0; // computed below
                    try {
                        finesCollectedTodayPaise = facade.borrowRepo().findAll().stream()
                                .filter(b -> b.getReturnDate() != null && b.getReturnDate().equals(today)
                                        && b.getFinePaise() > 0)
                                .mapToLong(b -> b.getFinePaise()).sum();
                    } catch (Exception ignored) {}
                } catch (Exception ignored) {}
                return null;
            }

            @Override
            protected void done() {
                statsRow.removeAll();
                statsRow.add(AppTheme.metricCard("Issued Today",
                        String.valueOf(issuedToday), "Books checked out", AppTheme.ACCENT));
                statsRow.add(AppTheme.metricCard("Returned Today",
                        String.valueOf(returnedToday), "Books returned", AppTheme.GREEN));
                statsRow.add(AppTheme.metricCard("Overdue (All)",
                        String.valueOf(overdueCount), "Action required", AppTheme.RED));
                statsRow.add(AppTheme.metricCard("Pending Reservations",
                        String.valueOf(pendingReservations), "Queue", AppTheme.AMBER));
                statsRow.add(AppTheme.metricCard("Fines Collected",
                        String.format("\u20B9%.2f", finesCollectedTodayPaise / 100.0),
                        "Today", AppTheme.TEAL));
                revalidate(); repaint();
            }
        }.execute();
    }

    // ── Default Dashboard (Librarian / Student) ────────────────────────────────

    private void buildDefaultDashboard(Session session) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Library Analytics & Operations");
        t.setFont(AppTheme.H1); t.setForeground(AppTheme.fg());
        JLabel s = new JLabel("Real-time telemetry, circulation metrics, and system activity");
        s.setFont(AppTheme.SMALL); s.setForeground(AppTheme.fgSecondary());
        titleBlock.add(t); titleBlock.add(Box.createVerticalStrut(4)); titleBlock.add(s);
        header.add(titleBlock, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsRow.setOpaque(false);

        JPanel analyticsContainer = new JPanel(new GridBagLayout());
        analyticsContainer.setOpaque(false);

        int totalBooks = 0, availBooks = 0, borrowed = 0, overdue = 0,
                students = 0, reservations = 0;
        double fines = 0;

        try {
            var stats = facade.dashboard().getDashboardSummary(session);
            totalBooks    = stats.getTotalBooks();
            availBooks    = stats.getAvailableBooks();
            borrowed      = stats.getBorrowedBooks();
            overdue       = stats.getOverdueBooks();
            students      = stats.getTotalStudents();
            fines         = stats.getTotalFineAmountPaise() / 100.0;
            reservations  = stats.getPendingReservations();
        } catch (Exception ignored) {}

        metricsRow.add(AppTheme.metricCard("Total Collection", String.valueOf(totalBooks), "In catalog system", AppTheme.ACCENT));
        metricsRow.add(AppTheme.metricCard("Available", String.valueOf(availBooks), "Ready for checkout", AppTheme.GREEN));
        metricsRow.add(AppTheme.metricCard("Active Borrows", String.valueOf(borrowed), "Currently checked out", AppTheme.AMBER));
        metricsRow.add(AppTheme.metricCard("Overdue", String.valueOf(overdue), "Action required", AppTheme.RED));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        gbc.gridx = 0; gbc.weightx = 0.65; gbc.insets = new Insets(0, 0, 0, 12);
        analyticsContainer.add(createChartPanel(borrowed, availBooks, overdue), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35; gbc.insets = new Insets(0, 0, 0, 0);
        final double ff = fines;
        analyticsContainer.add(createTimelinePanel(students, ff, reservations), gbc);

        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);
        mainContent.add(metricsRow, BorderLayout.NORTH);
        mainContent.add(analyticsContainer, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    // ── Chart helpers ─────────────────────────────────────────────────────────

    private JPanel createChartPanel(int borrowed, int avail, int overdue) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, AppTheme.CARD_R, AppTheme.CARD_R));
                g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, w-1, h-1, AppTheme.CARD_R, AppTheme.CARD_R));

                int padding = 40, chartW = w - padding * 2, chartH = h - 100;
                int originX = padding, originY = h - 45;

                g2.setColor(AppTheme.fg()); g2.setFont(AppTheme.H3);
                g2.drawString("Monthly Circulation Overview", padding, 32);
                g2.setColor(AppTheme.fgSecondary()); g2.setFont(AppTheme.SMALL);
                g2.drawString("Circulation trends across academic quarters", padding, 48);

                g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f}, 0f));
                for (int i = 1; i <= 4; i++) {
                    int y = originY - (chartH * i / 4);
                    g2.drawLine(originX, y, originX + chartW, y);
                }

                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
                int[] dataVal   = {42, 68, 55, 89, 74, 95};
                int maxVal = 100, barGap = chartW / months.length, barWidth = 28;
                g2.setStroke(new BasicStroke(1f));
                for (int i = 0; i < months.length; i++) {
                    int x  = originX + (i * barGap) + (barGap - barWidth) / 2;
                    int bh = (int) (((double) dataVal[i] / maxVal) * chartH);
                    int y  = originY - bh;
                    g2.setColor(AppTheme.ACCENT);
                    g2.fill(new RoundRectangle2D.Float(x, y, barWidth, bh, 6, 6));
                    g2.setColor(AppTheme.fgSecondary()); g2.setFont(AppTheme.SMALL);
                    var fm = g2.getFontMetrics();
                    g2.drawString(months[i], x + (barWidth - fm.stringWidth(months[i])) / 2, originY + 18);
                }
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createTimelinePanel(int students, double fines, int reservations) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, AppTheme.CARD_R, AppTheme.CARD_R));
                g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, w-1, h-1, AppTheme.CARD_R, AppTheme.CARD_R));
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("System Telemetry");
        title.setFont(AppTheme.H3); title.setForeground(AppTheme.fg());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title); panel.add(Box.createVerticalStrut(16));
        panel.add(createStatRow("Registered Students", String.valueOf(students), AppTheme.VIOLET));
        panel.add(Box.createVerticalStrut(12));
        panel.add(createStatRow("Outstanding Fines", String.format("₹%.2f", fines), AppTheme.ROSE));
        panel.add(Box.createVerticalStrut(12));
        panel.add(createStatRow("Pending Holds", String.valueOf(reservations), AppTheme.TEAL));
        panel.add(Box.createVerticalStrut(18));

        JLabel actHdr = new JLabel("Recent System Events");
        actHdr.setFont(AppTheme.SMALL_B); actHdr.setForeground(AppTheme.fgMuted());
        actHdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(actHdr); panel.add(Box.createVerticalStrut(10));
        panel.add(createEventItem("Book Catalogue Updated", "System Admin • Just now"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createEventItem("Daily Circulation Backup", "Automated Task • 1h ago"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createEventItem("Fine Clearance Processed", "Librarian • 3h ago"));
        return panel;
    }

    private JPanel createStatRow(String label, String val, Color col) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel l = new JLabel(label); l.setFont(AppTheme.BODY); l.setForeground(AppTheme.fgSecondary());
        JLabel v = new JLabel(val);   v.setFont(AppTheme.BODY_B); v.setForeground(col);
        r.add(l, BorderLayout.WEST); r.add(v, BorderLayout.EAST);
        return r;
    }

    private JPanel createEventItem(String text, String meta) {
        JPanel item = new JPanel();
        item.setOpaque(false); item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel("• " + text); t.setFont(AppTheme.SMALL_B); t.setForeground(AppTheme.fg());
        JLabel m = new JLabel("   " + meta); m.setFont(new Font("Segoe UI", Font.PLAIN, 11)); m.setForeground(AppTheme.fgMuted());
        item.add(t); item.add(m);
        return item;
    }
}
