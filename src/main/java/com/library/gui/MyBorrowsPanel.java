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
 * Executive My Borrows Panel — Student view of active and past book loans.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
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
        title.add(AppTheme.heading("My Borrowed Books"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Detailed log of books currently checked out or previously returned"));

        JButton refreshBtn = AppTheme.secondaryBtn("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 38));
        refreshBtn.addActionListener(e -> refresh(session));

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(refreshBtn);

        hdr.add(title, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        AppTheme.styleTable(table);

        // Status column pill renderer
        table.getColumnModel().getColumn(4).setCellRenderer((tbl, val, isSelected, hasFocus, row, col) -> {
            String statusStr = val != null ? val.toString() : "UNKNOWN";
            JPanel pill = AppTheme.createStatusPill(statusStr);
            if (isSelected) {
                pill.setOpaque(true);
                pill.setBackground(tbl.getSelectionBackground());
            } else {
                pill.setOpaque(false);
            }
            return pill;
        });

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false);
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH);
        add(tbl, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        model.setRowCount(0);

        Student student = facade.userRepo().findStudentByUsername(s.username());
        if (student == null) return;

        List<BorrowRecord> all = facade.borrowRepo().findByRegistrationNumber(student.getRegistrationNumber());
        for (BorrowRecord r : all) {
            String status = switch (r.getStatus()) {
                case ACTIVE         -> r.isOverdue() ? "OVERDUE" : "BORROWED";
                case RETURNED       -> "RETURNED";
                case RETURNED_LATE  -> "RETURNED (LATE)";
                case LOST           -> "LOST";
                default             -> r.getStatus().name();
            };
            String remain;
            if (r.getStatus().name().startsWith("RETURNED")) remain = "—";
            else remain = r.isOverdue() ? r.overdueDays() + "d overdue" : r.remainingDays() + " days";
            model.addRow(new Object[]{
                    r.getId(),
                    r.getBookId(),
                    r.getIssueDate().toString(),
                    r.getDueDate().toString(),
                    status,
                    remain
            });
        }
    }
}
