package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.security.Session;
import com.library.service.DashboardService;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Dashboard — key performance metrics at a glance.
 */
public final class DashboardPanel extends JPanel {

    private final LibraryFacade facade;
    private JPanel metricsRow, recentRow;

    public DashboardPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        build();
    }

    private void build() {
        removeAll();
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Dashboard");
        t.setFont(AppTheme.H1); t.setForeground(AppTheme.fg());
        JLabel s = new JLabel("Overview of library operations and key metrics");
        s.setFont(AppTheme.SMALL); s.setForeground(AppTheme.fgSecondary());
        header.add(t); header.add(Box.createVerticalStrut(4)); header.add(s);
        add(header, BorderLayout.NORTH);

        metricsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsRow.setOpaque(false);
        metricsRow.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        recentRow = new JPanel(new GridLayout(1, 3, 16, 0));
        recentRow.setOpaque(false);
        recentRow.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(metricsRow, BorderLayout.NORTH);
        body.add(recentRow, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        setBackground(AppTheme.bg());
        metricsRow.removeAll(); recentRow.removeAll();
        try {
            var stats = facade.dashboard().getDashboardSummary(session);
            metricsRow.add(AppTheme.metricCard("Total Books",      String.valueOf(stats.getTotalBooks()),      AppTheme.ACCENT));
            metricsRow.add(AppTheme.metricCard("Available",         String.valueOf(stats.getAvailableBooks()),  AppTheme.GREEN));
            metricsRow.add(AppTheme.metricCard("Active Borrows",    String.valueOf(stats.getBorrowedBooks()),   AppTheme.AMBER));
            metricsRow.add(AppTheme.metricCard("Overdue",           String.valueOf(stats.getOverdueBooks()),    AppTheme.RED));

            recentRow.add(infoCard("Registered Students",  String.valueOf(stats.getTotalStudents()),   AppTheme.VIOLET));
            recentRow.add(infoCard("Pending Fines",        String.format("\u20B9%.0f", stats.getTotalFineAmountPaise() / 100.0), AppTheme.ROSE));
            recentRow.add(infoCard("Reservations",         String.valueOf(stats.getPendingReservations()), AppTheme.TEAL));
        } catch (Exception ex) {
            metricsRow.add(AppTheme.metricCard("Total Books",   "0", AppTheme.ACCENT));
            metricsRow.add(AppTheme.metricCard("Available",     "0", AppTheme.GREEN));
            metricsRow.add(AppTheme.metricCard("Active Borrows","0", AppTheme.AMBER));
            metricsRow.add(AppTheme.metricCard("Overdue",       "0", AppTheme.RED));
        }
        revalidate(); repaint();
    }

    private JPanel infoCard(String title, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, AppTheme.isDark() ? 25 : 8));
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth()-2, getHeight()-2, 14, 14));
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-2, getHeight()-2, 14, 14));
                if (!AppTheme.isDark()) {
                    g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-3, getHeight()-3, 14, 14));
                }
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // accent dot + title
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        JLabel dot = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g);
                g.setColor(accent); ((Graphics2D)g).fillOval(0, 2, 10, 10);
            }
        };
        dot.setPreferredSize(new Dimension(10, 14));
        JLabel tl = new JLabel(title.toUpperCase());
        tl.setFont(AppTheme.SMALL_B); tl.setForeground(AppTheme.fgMuted());
        row.add(dot); row.add(tl);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel vl = new JLabel(value);
        vl.setFont(AppTheme.METRIC); vl.setForeground(AppTheme.fg());
        vl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(row); card.add(Box.createVerticalStrut(12)); card.add(vl);
        return card;
    }
}
