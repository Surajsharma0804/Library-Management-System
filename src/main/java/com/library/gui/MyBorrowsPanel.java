package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.BorrowRecord;
import com.library.model.Student;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Student-specific borrow view — shows only the logged-in
 * student's active and past borrows.
 *
 * @author University Central Library — Software Engineering Division
 */
public final class MyBorrowsPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel model;
    private Session session;

    private static final String[] COLS = {"Borrow ID", "Book ID", "Issue Date", "Due Date", "Status", "Days Remaining"};

    public MyBorrowsPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("My Borrows"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Books you have currently borrowed or previously returned"));

        JButton refreshBtn = AppTheme.secondaryBtn("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 40));
        refreshBtn.addActionListener(e -> refresh(session));

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(refreshBtn);
        hdr.add(title, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rn, int r, int c) {
                Component comp = super.prepareRenderer(rn, r, c);
                if (!isRowSelected(r)) comp.setBackground(r % 2 == 0 ? AppTheme.bgCard() : AppTheme.tableAlt());
                else comp.setBackground(new Color(AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(), AppTheme.ACCENT.getBlue(), 40));
                comp.setForeground(AppTheme.fg());
                if (c == 4) {
                    String v = String.valueOf(getValueAt(r, c));
                    if ("ACTIVE".equals(v)) comp.setForeground(AppTheme.GREEN);
                    else if ("OVERDUE".equals(v)) comp.setForeground(AppTheme.RED);
                    else if ("RETURNED".equals(v)) comp.setForeground(AppTheme.fgMuted());
                }
                return comp;
            }
        };
        AppTheme.styleTable(table);

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false); tbl.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH); add(tbl, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        model.setRowCount(0);

        Student student = facade.userRepo().findStudentByUsername(s.username());
        if (student == null) return;

        List<BorrowRecord> all = facade.borrowRepo().findByRegistrationNumber(student.getRegistrationNumber());
        for (BorrowRecord r : all) {
            String status = r.getStatus().name();
            if (r.isOverdue() && "ACTIVE".equals(status)) status = "OVERDUE";
            String remain;
            if ("RETURNED".equals(r.getStatus().name())) remain = "-";
            else remain = r.isOverdue() ? r.overdueDays() + "d overdue" : r.remainingDays() + " days";
            model.addRow(new Object[]{r.getId(), r.getBookId(), r.getIssueDate().toString(),
                    r.getDueDate().toString(), status, remain});
        }
    }
}
