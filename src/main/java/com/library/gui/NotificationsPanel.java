package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Notification;
import com.library.model.Student;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Notifications panel — students view their own notifications.
 * Columns: Timestamp, Type, Message.
 * Unread notifications are rendered in bold. Clicking a row marks it as read
 * and shows the full message in a JTextArea below the table.
 */
public final class NotificationsPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel model;
    private Session session;
    private List<Notification> notifications = List.of();

    private JTextArea detailArea;

    private static final String[] COLS = {"Timestamp", "Type", "Message"};

    public NotificationsPanel(LibraryFacade facade) {
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
        title.add(AppTheme.heading("Notifications"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("System messages and library alerts for your account"));

        JButton refreshBtn = AppTheme.secondaryBtn("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 38));
        refreshBtn.addActionListener(e -> { if (session != null) refresh(session); });

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(refreshBtn);
        hdr.add(title, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        AppTheme.styleTable(table);

        // Bold renderer for unread notifications
        DefaultTableCellRenderer boldRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component comp = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? AppTheme.bgCard() : AppTheme.tableAlt());
                } else {
                    comp.setBackground(new Color(
                            AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(),
                            AppTheme.ACCENT.getBlue(), 40));
                }
                comp.setForeground(AppTheme.fg());
                if (notifications != null && row < notifications.size()) {
                    Notification n = notifications.get(row);
                    comp.setFont(n.isRead()
                            ? comp.getFont().deriveFont(Font.PLAIN)
                            : comp.getFont().deriveFont(Font.BOLD));
                }
                return comp;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(boldRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(boldRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(boldRenderer);

        // Click row → mark read, show full message in detail area
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || notifications == null || row >= notifications.size()) return;
                Notification n = notifications.get(row);
                if (!n.isRead()) {
                    n.setRead(true);
                    facade.notificationRepo().save(n);
                    table.repaint();
                }
                detailArea.setText(
                        "Type:    " + n.getType().name() + "\n"
                        + "Time:    " + n.getCreatedAt() + "\n\n"
                        + n.getMessage());
                detailArea.setCaretPosition(0);
            }
        });

        // Detail area below table
        detailArea = new JTextArea(5, 0);
        detailArea.setEditable(false);
        detailArea.setFont(AppTheme.BODY);
        detailArea.setForeground(AppTheme.fg());
        detailArea.setBackground(AppTheme.bgCard());
        detailArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.border()),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setText("Select a notification to view its full content.");
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createEmptyBorder());
        detailScroll.setPreferredSize(new Dimension(0, 110));

        JLabel detailLabel = new JLabel("Full Message");
        detailLabel.setFont(AppTheme.SMALL_B);
        detailLabel.setForeground(AppTheme.fgSecondary());

        JPanel detailPanel = new JPanel(new BorderLayout(0, 4));
        detailPanel.setOpaque(false);
        detailPanel.add(detailLabel, BorderLayout.NORTH);
        detailPanel.add(detailScroll, BorderLayout.CENTER);

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false);
        tbl.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(tbl, BorderLayout.CENTER);
        center.add(detailPanel, BorderLayout.SOUTH);

        add(hdr, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        model.setRowCount(0);
        notifications = List.of();
        detailArea.setText("Select a notification to view its full content.");

        new SwingWorker<List<Notification>, Void>() {
            @Override
            protected List<Notification> doInBackground() {
                try {
                    Student student = facade.userRepo().findStudentByUsername(s.username());
                    if (student == null) return List.of();
                    return facade.notificationRepo().findByStudent(student.getRegistrationNumber())
                            .stream()
                            .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                            .toList();
                } catch (Exception e) {
                    return List.of();
                }
            }

            @Override
            protected void done() {
                try {
                    notifications = get();
                    model.setRowCount(0);
                    for (Notification n : notifications) {
                        String msg = n.getMessage();
                        String display = msg.length() > 80 ? msg.substring(0, 77) + "..." : msg;
                        model.addRow(new Object[]{
                                n.getCreatedAt() != null ? n.getCreatedAt().toString() : "-",
                                n.getType().name(),
                                display
                        });
                    }
                } catch (Exception e) {
                    AppTheme.error(NotificationsPanel.this, "Failed to load notifications: " + e.getMessage());
                }
                revalidate(); repaint();
            }
        }.execute();
    }
}
