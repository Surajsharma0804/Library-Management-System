package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.BorrowRecord;
import com.library.model.Student;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Student home dashboard — personal overview of borrows, fines,
 * remaining quota, and membership status.
 *
 * @author University Central Library — Software Engineering Division
 */
public final class StudentHomePanel extends JPanel {

    private final LibraryFacade facade;
    private JPanel metricsRow, infoRow;

    public StudentHomePanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        build();
    }

    private void build() {
        removeAll();
        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Welcome Back");
        t.setFont(AppTheme.H1); t.setForeground(AppTheme.fg());
        JLabel s = new JLabel("Your library account overview");
        s.setFont(AppTheme.SMALL); s.setForeground(AppTheme.fgSecondary());
        hdr.add(t); hdr.add(Box.createVerticalStrut(4)); hdr.add(s);
        add(hdr, BorderLayout.NORTH);

        metricsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsRow.setOpaque(false);
        metricsRow.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        infoRow = new JPanel(new GridLayout(1, 2, 16, 0));
        infoRow.setOpaque(false);
        infoRow.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(metricsRow, BorderLayout.NORTH);
        body.add(infoRow, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        setBackground(AppTheme.bg());
        metricsRow.removeAll(); infoRow.removeAll();

        Student student = facade.userRepo().findStudentByUsername(session.username());
        if (student == null) {
            metricsRow.add(AppTheme.metricCard("Error", "N/A", AppTheme.RED));
            revalidate(); repaint();
            return;
        }

        List<BorrowRecord> borrows = facade.borrowRepo().findActiveByRegistrationNumber(student.getRegistrationNumber());
        long overdue = borrows.stream().filter(BorrowRecord::isOverdue).count();
        long fineBalance = student.getFineBalancePaise();

        metricsRow.add(AppTheme.metricCard("Active Borrows",  String.valueOf(borrows.size()),       AppTheme.ACCENT));
        metricsRow.add(AppTheme.metricCard("Overdue Books",   String.valueOf(overdue),              AppTheme.RED));
        metricsRow.add(AppTheme.metricCard("Remaining Quota", String.valueOf(student.remainingBorrowSlots()), AppTheme.GREEN));
        metricsRow.add(AppTheme.metricCard("Fine Balance",    String.format("\u20B9%.0f", fineBalance / 100.0), AppTheme.AMBER));

        // info cards
        infoRow.add(detailCard("Account Information", new String[][]{
                {"Name", student.getFirstName() + " " + student.getLastName()},
                {"Registration No.", student.getRegistrationNumber()},
                {"Department", student.getDepartment() != null ? student.getDepartment() : "-"},
                {"Course", student.getCourse() != null ? student.getCourse() : "-"},
                {"Semester", String.valueOf(student.getSemester())},
                {"Status", student.getMembershipStatus().name()},
        }));

        infoRow.add(detailCard("Current Borrows", borrows.isEmpty()
                ? new String[][]{{"", "No active borrows"}}
                : borrows.stream().map(b -> new String[]{
                    b.getBookId(), "Due: " + b.getDueDate() + (b.isOverdue() ? "  \u26A0 OVERDUE" : "")
                }).toArray(String[][]::new)
        ));

        revalidate(); repaint();
    }

    private JPanel detailCard(String title, String[][] rows) {
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
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.H3); titleLbl.setForeground(AppTheme.fg());
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(14));

        for (String[] row : rows) {
            JPanel r = new JPanel(new BorderLayout());
            r.setOpaque(false);
            r.setAlignmentX(Component.LEFT_ALIGNMENT);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

            JLabel k = new JLabel(row[0]);
            k.setFont(AppTheme.SMALL); k.setForeground(AppTheme.fgMuted());
            JLabel v = new JLabel(row[1]);
            v.setFont(AppTheme.BODY); v.setForeground(AppTheme.fg());
            if (row[1].contains("OVERDUE")) v.setForeground(AppTheme.RED);
            if ("ACTIVE".equals(row[1])) v.setForeground(AppTheme.GREEN);

            r.add(k, BorderLayout.WEST);
            r.add(v, BorderLayout.EAST);
            card.add(r);
            card.add(Box.createVerticalStrut(4));
        }

        return card;
    }
}
