package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.BorrowRecord;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Circulation panel — issue, return, and renew book borrows.
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
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("Circulation"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Issue, return, and renew book borrows"));

        JButton issueBtn  = AppTheme.primaryBtn("Issue Book");
        JButton returnBtn = AppTheme.secondaryBtn("Return Book");
        JButton renewBtn  = AppTheme.secondaryBtn("Renew");
        issueBtn.setPreferredSize(new Dimension(120, 40));
        returnBtn.setPreferredSize(new Dimension(120, 40));
        renewBtn.setPreferredSize(new Dimension(100, 40));
        issueBtn.addActionListener(e -> issueBook());
        returnBtn.addActionListener(e -> returnBook());
        renewBtn.addActionListener(e -> renewBook());

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(issueBtn); acts.add(returnBtn); acts.add(renewBtn);
        hdr.add(title, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rn, int r, int c) {
                Component comp = super.prepareRenderer(rn, r, c);
                if (!isRowSelected(r)) comp.setBackground(r % 2 == 0 ? AppTheme.bgCard() : AppTheme.tableAlt());
                else comp.setBackground(new Color(AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(), AppTheme.ACCENT.getBlue(), 40));
                comp.setForeground(AppTheme.fg());
                if (c == 5) { String v = String.valueOf(getValueAt(r, c));
                    if ("ACTIVE".equals(v)) comp.setForeground(AppTheme.GREEN);
                    else if ("OVERDUE".equals(v)) comp.setForeground(AppTheme.RED);
                    else comp.setForeground(AppTheme.fgMuted());
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
        this.session = s; setBackground(AppTheme.bg());
        model.setRowCount(0);
        List<BorrowRecord> recs = facade.borrows().findAllActive();
        for (BorrowRecord r : recs) {
            String status = r.isOverdue() ? "OVERDUE" : r.getStatus().name();
            String remain = r.isOverdue() ? r.overdueDays() + "d overdue" : r.remainingDays() + " days";
            model.addRow(new Object[]{r.getId(), r.getBookId(), r.getRegistrationNumber(),
                    r.getIssueDate().toString(), r.getDueDate().toString(), status, remain});
        }
    }

    private void issueBook() {
        if (session == null) return;
        JPanel f = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField bk = new JTextField(), st = new JTextField();
        f.add(new JLabel("Book ID:")); f.add(bk);
        f.add(new JLabel("Student Reg No:")); f.add(st);
        if (JOptionPane.showConfirmDialog(this, f, "Issue Book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                facade.borrows().issueBook(session, bk.getText().trim(), st.getText().trim());
                refresh(session);
                AppTheme.success(this, "Book issued successfully!");
            } catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
        }
    }

    private void returnBook() {
        if (session == null) return;
        int row = table.getSelectedRow();
        String id = row >= 0 ? (String) model.getValueAt(row, 0) :
                JOptionPane.showInputDialog(this, "Enter Borrow ID:");
        if (id == null || id.trim().isEmpty()) return;
        try {
            facade.borrows().returnBook(session, id.trim());
            refresh(session);
            AppTheme.success(this, "Book returned successfully!");
        } catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
    }

    private void renewBook() {
        if (session == null) return;
        int row = table.getSelectedRow();
        String id = row >= 0 ? (String) model.getValueAt(row, 0) :
                JOptionPane.showInputDialog(this, "Enter Borrow ID:");
        if (id == null || id.trim().isEmpty()) return;
        try {
            facade.borrows().renewBook(session, id.trim());
            refresh(session);
            AppTheme.success(this, "Borrow renewed successfully!");
        } catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
    }
}
