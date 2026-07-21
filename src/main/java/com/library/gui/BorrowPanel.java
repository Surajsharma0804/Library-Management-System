package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.BorrowRecord;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Borrow and return panel — issue books, return books, view active borrows.
 */
public final class BorrowPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel tableModel;
    private Session session;

    private static final String[] COLUMNS = {"Borrow ID", "Book ID", "Student Reg", "Issue Date", "Due Date", "Status", "Days Left"};

    public BorrowPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(AppTheme.heading("Borrow & Return"));
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(AppTheme.secondaryLabel("Issue and return books, manage active borrows"));

        JButton issueBtn = AppTheme.primaryButton("Issue Book");
        issueBtn.setPreferredSize(new Dimension(130, 40));
        issueBtn.addActionListener(e -> showIssueDialog());

        JButton returnBtn = AppTheme.secondaryButton("Return Book");
        returnBtn.setPreferredSize(new Dimension(130, 40));
        returnBtn.addActionListener(e -> showReturnDialog());

        JButton renewBtn = AppTheme.secondaryButton("Renew");
        renewBtn.setPreferredSize(new Dimension(100, 40));
        renewBtn.addActionListener(e -> showRenewDialog());

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(issueBtn);
        actionsPanel.add(returnBtn);
        actionsPanel.add(renewBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? AppTheme.BG_SECONDARY : AppTheme.TABLE_ROW_ALT);
                } else { c.setBackground(AppTheme.ACCENT_DARK); }
                c.setForeground(AppTheme.TEXT_PRIMARY);
                if (col == 5) {
                    String val = String.valueOf(getValueAt(row, col));
                    switch (val) {
                        case "ACTIVE" -> c.setForeground(AppTheme.SUCCESS);
                        case "RETURNED_LATE" -> c.setForeground(AppTheme.DANGER);
                        case "RETURNED" -> c.setForeground(AppTheme.TEXT_MUTED);
                        default -> c.setForeground(AppTheme.WARNING);
                    }
                }
                if (col == 6) {
                    String val = String.valueOf(getValueAt(row, col));
                    try {
                        long days = Long.parseLong(val);
                        if (days <= 2) c.setForeground(AppTheme.DANGER);
                        else if (days <= 5) c.setForeground(AppTheme.WARNING);
                        else c.setForeground(AppTheme.SUCCESS);
                    } catch (NumberFormatException ignored) {}
                }
                return c;
            }
        };
        AppTheme.styleTable(table);

        JScrollPane sp = AppTheme.styledScrollPane(table);
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        tableContainer.add(sp, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        this.session = session;
        tableModel.setRowCount(0);
        List<BorrowRecord> records = facade.borrowRepo().findAll();
        // Show most recent first
        records.stream()
                .sorted((a, b) -> {
                    if (a.getIssueDate() == null || b.getIssueDate() == null) return 0;
                    return b.getIssueDate().compareTo(a.getIssueDate());
                })
                .forEach(r -> tableModel.addRow(new Object[]{
                        r.getId(), r.getBookId(), r.getRegistrationNumber(),
                        r.getIssueDate(), r.getDueDate(),
                        r.getStatus().name(),
                        r.getStatus() == com.library.enums.BorrowStatus.ACTIVE ? r.remainingDays() : "-"
                }));
    }

    private void showIssueDialog() {
        if (session == null) return;
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField bookIdF = new JTextField(); JTextField regF = new JTextField();
        form.add(lbl("Book ID:")); form.add(bookIdF);
        form.add(lbl("Student Reg No:")); form.add(regF);

        int result = JOptionPane.showConfirmDialog(this, form, "Issue Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                facade.borrows().issueBook(session, bookIdF.getText().trim(), regF.getText().trim());
                refresh(session);
                AppTheme.showSuccess(this, "Book issued successfully!");
            } catch (Exception ex) {
                AppTheme.showError(this, ex.getMessage());
            }
        }
    }

    private void showReturnDialog() {
        if (session == null) return;
        int row = table.getSelectedRow();
        String borrowId;
        if (row >= 0) {
            borrowId = (String) tableModel.getValueAt(row, 0);
        } else {
            borrowId = JOptionPane.showInputDialog(this, "Enter Borrow ID:");
            if (borrowId == null || borrowId.trim().isEmpty()) return;
        }
        try {
            facade.borrows().returnBook(session, borrowId.trim());
            refresh(session);
            AppTheme.showSuccess(this, "Book returned successfully!");
        } catch (Exception ex) {
            AppTheme.showError(this, ex.getMessage());
        }
    }

    private void showRenewDialog() {
        if (session == null) return;
        int row = table.getSelectedRow();
        String borrowId;
        if (row >= 0) {
            borrowId = (String) tableModel.getValueAt(row, 0);
        } else {
            borrowId = JOptionPane.showInputDialog(this, "Enter Borrow ID:");
            if (borrowId == null || borrowId.trim().isEmpty()) return;
        }
        try {
            facade.borrows().renewBook(session, borrowId.trim());
            refresh(session);
            AppTheme.showSuccess(this, "Borrow renewed successfully!");
        } catch (Exception ex) {
            AppTheme.showError(this, ex.getMessage());
        }
    }

    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setFont(AppTheme.FONT_BODY); return l; }
}
