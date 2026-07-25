package com.library.gui;

import com.library.enums.ReservationStatus;
import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Notification;
import com.library.model.Student;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Executive Student Dashboard — 5 metric tiles (Active Borrows, Overdue,
 * Pending Reservations, Outstanding Fine, Unread Notifications) plus a
 * personalised recommendations section.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class StudentHomePanel extends JPanel {

    private final LibraryFacade facade;
    private JPanel metricsRow;
    private JPanel centerPanel;

    public StudentHomePanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        removeAll();

        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));

        JLabel t = new JLabel("Student Portal");
        t.setFont(AppTheme.H1); t.setForeground(AppTheme.fg());

        JLabel s = new JLabel("Personal library account overview, active loans, and notifications");
        s.setFont(AppTheme.SMALL); s.setForeground(AppTheme.fgSecondary());

        hdr.add(t); hdr.add(Box.createVerticalStrut(4)); hdr.add(s);
        add(hdr, BorderLayout.NORTH);

        // 5 metric tiles (GridLayout 1×5)
        metricsRow = new JPanel(new GridLayout(1, 5, 14, 0));
        metricsRow.setOpaque(false);

        // Placeholder loading tiles
        for (int i = 0; i < 5; i++) {
            metricsRow.add(AppTheme.metricCard("Loading…", "—", AppTheme.ACCENT));
        }

        // Recommendations panel
        JPanel recoCard = createCard("Recommended for You");
        centerPanel = recoCard;

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(metricsRow, BorderLayout.NORTH);
        body.add(centerPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        setBackground(AppTheme.bg());

        // Show loading placeholders
        metricsRow.removeAll();
        for (int i = 0; i < 5; i++) {
            metricsRow.add(AppTheme.metricCard("Loading…", "—", AppTheme.ACCENT));
        }
        metricsRow.revalidate();

        // Single background worker for both metrics and recommendations
        new SwingWorker<Void, Void>() {
            String[] vals = new String[5];
            List<Book> recommendations = List.of();

            @Override
            protected Void doInBackground() {
                try {
                    Student student = facade.userRepo().findStudentByUsername(session.username());
                    if (student == null) {
                        for (int i = 0; i < 5; i++) vals[i] = "Error";
                        return null;
                    }
                    String regNo = student.getRegistrationNumber();

                    // 1. Active borrows (reuse query for overdue count)
                    try {
                        List<BorrowRecord> borrows = facade.borrowRepo()
                                .findActiveByRegistrationNumber(regNo);
                        vals[0] = String.valueOf(borrows.size());
                        long overdue = borrows.stream().filter(BorrowRecord::isOverdue).count();
                        vals[1] = String.valueOf(overdue);
                    } catch (Exception e) { vals[0] = "Error"; vals[1] = "Error"; }

                    // 3. Pending reservations
                    try {
                        long pending = facade.reservations().findByStudent(regNo).stream()
                                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                                .count();
                        vals[2] = String.valueOf(pending);
                    } catch (Exception e) { vals[2] = "Error"; }

                    // 4. Outstanding fine (use already-loaded student instead of re-querying)
                    try {
                        vals[3] = String.format("₹%.2f", student.getFineBalancePaise() / 100.0);
                    } catch (Exception e) { vals[3] = "Error"; }

                    // 5. Unread notifications
                    try {
                        long unread = facade.notificationRepo()
                                .findByStudent(regNo).stream()
                                .filter(n -> !n.isRead()).count();
                        vals[4] = String.valueOf(unread);
                    } catch (Exception e) { vals[4] = "Error"; }

                    // 6. Recommendations
                    try {
                        recommendations = facade.recommendations().recommend(regNo)
                                .stream().limit(5).toList();
                    } catch (Exception e) {
                        recommendations = List.of();
                    }

                } catch (Exception e) {
                    for (int i = 0; i < 5; i++) if (vals[i] == null) vals[i] = "Error";
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ignored) { return; }

                // Update metrics
                metricsRow.removeAll();
                Color c0 = "Error".equals(vals[0]) ? AppTheme.RED : AppTheme.ACCENT;
                Color c1 = "Error".equals(vals[1]) || !"0".equals(vals[1]) ? AppTheme.RED : AppTheme.GREEN;
                Color c2 = "Error".equals(vals[2]) ? AppTheme.RED : AppTheme.AMBER;
                Color c3 = "Error".equals(vals[3]) ? AppTheme.RED : AppTheme.AMBER;
                Color c4 = "Error".equals(vals[4]) ? AppTheme.RED : AppTheme.ACCENT;

                metricsRow.add(AppTheme.metricCard("Active Borrows",        vals[0], "Books currently on loan",    c0));
                metricsRow.add(AppTheme.metricCard("Overdue Borrows",       vals[1], "Action required",            c1));
                metricsRow.add(AppTheme.metricCard("Pending Reservations",  vals[2], "In queue",                   c2));
                metricsRow.add(AppTheme.metricCard("Outstanding Fine",      vals[3], "Balance due",                c3));
                metricsRow.add(AppTheme.metricCard("Unread Notifications",  vals[4], "New messages",               c4));
                metricsRow.revalidate();

                // Update recommendations
                Container parent = centerPanel.getParent();
                if (parent != null) {
                    parent.remove(centerPanel);
                    centerPanel = buildRecommendationsCard(recommendations);
                    parent.add(centerPanel, BorderLayout.CENTER);
                    parent.revalidate();
                    parent.repaint();
                }
            }
        }.execute();
    }

    private JPanel buildRecommendationsCard(List<Book> books) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        AppTheme.CARD_R, AppTheme.CARD_R));
                g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1,
                        AppTheme.CARD_R, AppTheme.CARD_R));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel("Recommended for You");
        titleLbl.setFont(AppTheme.H3); titleLbl.setForeground(AppTheme.fg());
        card.add(titleLbl, BorderLayout.NORTH);

        if (books.isEmpty()) {
            JLabel none = new JLabel("No recommendations available yet.");
            none.setFont(AppTheme.BODY);
            none.setForeground(AppTheme.fgMuted());
            card.add(none, BorderLayout.CENTER);
            return card;
        }

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Book b : books) {
            listModel.addElement(b.getTitle() + "  —  " + b.getAuthor()
                    + (b.getCategory() != null ? "  [" + b.getCategory() + "]" : ""));
        }

        JList<String> list = new JList<>(listModel);
        list.setFont(AppTheme.BODY);
        list.setForeground(AppTheme.fg());
        list.setBackground(AppTheme.bgCard());
        list.setSelectionBackground(new Color(
                AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(), AppTheme.ACCENT.getBlue(), 40));
        list.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCard(String titleText) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        AppTheme.CARD_R, AppTheme.CARD_R));
                g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1,
                        AppTheme.CARD_R, AppTheme.CARD_R));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel(titleText);
        titleLbl.setFont(AppTheme.H3); titleLbl.setForeground(AppTheme.fg());
        card.add(titleLbl, BorderLayout.NORTH);

        JLabel loading = new JLabel("Loading recommendations…");
        loading.setFont(AppTheme.BODY); loading.setForeground(AppTheme.fgMuted());
        card.add(loading, BorderLayout.CENTER);

        return card;
    }
}
