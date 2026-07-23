package com.library.gui;

import com.library.enums.BorrowStatus;
import com.library.facade.LibraryFacade;
import com.library.model.BorrowRecord;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Executive Circulation Panel — Book issue, return, renewal, and due-date tracking.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class BorrowPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel model;
    private Session session;

    private static final String[] COLS = {"Borrow ID", "Book ID", "Student Reg", "Issue Date", "Due Date", "Status", "Remaining"};

    public BorrowPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        removeAll();

        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("Circulation Management"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Issue, return, and renew book circulation records"));

        JButton issueBtn  = AppTheme.primaryBtn("+ Issue Book");
        JButton returnBtn = AppTheme.secondaryBtn("Return Book");
        JButton renewBtn  = AppTheme.secondaryBtn("Renew Loan");
        JButton lostBtn   = AppTheme.dangerBtn("Mark Lost");
        issueBtn.setPreferredSize(new Dimension(130, 38));
        returnBtn.setPreferredSize(new Dimension(120, 38));
        renewBtn.setPreferredSize(new Dimension(110, 38));
        lostBtn.setPreferredSize(new Dimension(110, 38));

        issueBtn.addActionListener(e -> issueBook());
        returnBtn.addActionListener(e -> returnBook());
        renewBtn.addActionListener(e -> renewBook());
        lostBtn.addActionListener(e -> markLost());

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(issueBtn);
        acts.add(returnBtn);
        acts.add(renewBtn);
        acts.add(lostBtn);

        hdr.add(title, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        AppTheme.styleTable(table);

        // Status column pill renderer
        table.getColumnModel().getColumn(5).setCellRenderer((tbl, val, isSelected, hasFocus, row, col) -> {
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
        List<BorrowRecord> recs = facade.borrows().findAllActive();
        for (BorrowRecord r : recs) {
            String status = r.isOverdue() ? "OVERDUE" : r.getStatus().name();
            String remain = r.isOverdue() ? r.overdueDays() + "d overdue" : r.remainingDays() + " days";
            model.addRow(new Object[]{
                    r.getId(),
                    r.getBookId(),
                    r.getRegistrationNumber(),
                    r.getIssueDate().toString(),
                    r.getDueDate().toString(),
                    status,
                    remain
            });
        }
    }

    private void issueBook() {
        if (session == null) return;

        JPanel f = new JPanel(new GridLayout(2, 2, 12, 12));
        f.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField bk = AppTheme.textField(15);
        JTextField st = AppTheme.textField(15);

        f.add(lbl("Book ID:")); f.add(bk);
        f.add(lbl("Student Reg No:")); f.add(st);

        if (JOptionPane.showConfirmDialog(this, f, "Issue Book to Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                facade.borrows().issueBook(session, bk.getText().trim(), st.getText().trim());
                refresh(session);
                AppTheme.success(this, "Book successfully issued!");
            } catch (Exception ex) {
                AppTheme.error(this, ex.getMessage());
            }
        }
    }

    private void returnBook() {
        if (session == null) return;
        int row = table.getSelectedRow();
        String id = row >= 0 ? (String) model.getValueAt(row, 0) :
                JOptionPane.showInputDialog(this, "Enter Borrow Record ID:");
        if (id == null || id.trim().isEmpty()) return;
        try {
            facade.borrows().returnBook(session, id.trim());
            refresh(session);
            AppTheme.success(this, "Book returned successfully!");
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
        }
    }

    private void renewBook() {
        if (session == null) return;
        int row = table.getSelectedRow();
        String id = row >= 0 ? (String) model.getValueAt(row, 0) :
                JOptionPane.showInputDialog(this, "Enter Borrow Record ID:");
        if (id == null || id.trim().isEmpty()) return;
        try {
            facade.borrows().renewBook(session, id.trim());
            refresh(session);
            AppTheme.success(this, "Borrow record renewed successfully!");
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
        }
    }

    private void markLost() {
        if (session == null) return;

        // 1. Permission check
        try {
            facade.rbac().require(session, Permissions.BOOK_MARK_LOST);
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
            return;
        }

        // 2. Get selected borrow record
        int row = table.getSelectedRow();
        if (row < 0) {
            AppTheme.error(this, "Please select an active borrow record first.");
            return;
        }
        String borrowId = (String) model.getValueAt(row, 0);

        // 3. Find the borrow record and confirm it is ACTIVE
        BorrowRecord selected = facade.borrows().findAllActive().stream()
                .filter(b -> b.getId().equals(borrowId))
                .findFirst().orElse(null);
        if (selected == null || selected.getStatus() != BorrowStatus.ACTIVE) {
            AppTheme.error(this, "Selected borrow record is not ACTIVE.");
            return;
        }

        // 4. Replacement cost dialog
        JPanel f = new JPanel(new GridLayout(1, 2, 12, 12));
        f.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JTextField costField = AppTheme.textField(12);
        f.add(lbl("Replacement Cost (paise):")); f.add(costField);

        if (JOptionPane.showConfirmDialog(this, f, "Mark Book as Lost",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        // 5. Validate cost
        long costPaise;
        try {
            costPaise = Long.parseLong(costField.getText().trim());
            if (costPaise <= 0) throw new NumberFormatException("must be > 0");
        } catch (NumberFormatException ex) {
            AppTheme.error(this, "Replacement cost must be a positive number in paise.");
            return;
        }

        // 6. Call service
        try {
            facade.borrows().markLostBook(session, borrowId, costPaise);
            refresh(session);
            AppTheme.success(this, "Book marked as lost and fine raised successfully.");
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
        }
    }

    private JLabel lbl(String s) {
        var l = new JLabel(s);
        l.setFont(AppTheme.BODY_B);
        l.setForeground(AppTheme.fg());
        return l;
    }
}
