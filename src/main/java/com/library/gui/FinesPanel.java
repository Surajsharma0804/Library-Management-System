package com.library.gui;

import com.library.enums.FineStatus;
import com.library.facade.LibraryFacade;
import com.library.model.Fine;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Executive Fine Management Panel — Track, filter, and process fine payments.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class FinesPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel model;
    private Session session;

    private static final String[] COLS = {"Fine ID", "Student Reg", "Book ID", "Amount", "Reason", "Status", "Date"};

    public FinesPanel(LibraryFacade facade) {
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
        title.add(AppTheme.heading("Fine Management"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Track outstanding library balances and collect payments"));

        JButton payBtn = AppTheme.primaryBtn("Collect Fine");
        payBtn.setPreferredSize(new Dimension(130, 38));
        payBtn.addActionListener(e -> collectFine());

        JButton refBtn = AppTheme.secondaryBtn("Refresh");
        refBtn.setPreferredSize(new Dimension(100, 38));
        refBtn.addActionListener(e -> refresh(session));

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(payBtn);
        acts.add(refBtn);

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
        List<Fine> fines = facade.fineRepo().findAll();
        fines.stream()
                .sorted((a, b) -> {
                    boolean ap = a.getStatus() == FineStatus.PAID, bp = b.getStatus() == FineStatus.PAID;
                    return ap != bp ? (ap ? 1 : -1) : 0;
                })
                .forEach(f -> model.addRow(new Object[]{
                        f.getId(),
                        f.getRegistrationNumber(),
                        f.getBookId(),
                        String.format("₹%.2f", f.getAmountPaise() / 100.0),
                        f.getReason(),
                        f.getStatus() == FineStatus.PAID ? "PAID" : "UNPAID",
                        f.getCreatedAt() != null ? f.getCreatedAt().toLocalDate().toString() : "-"
                }));
    }

    private void collectFine() {
        if (session == null) return;
        int row = table.getSelectedRow();
        if (row < 0) {
            AppTheme.error(this, "Please select a fine record from the table to process payment.");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        String st = (String) model.getValueAt(row, 5);
        if ("PAID".equals(st)) {
            AppTheme.error(this, "This fine record has already been marked as PAID.");
            return;
        }
        String amt = (String) model.getValueAt(row, 3);
        if (AppTheme.confirm(this, "Confirm payment collection of " + amt + " for Fine ID " + id + "?")) {
            try {
                facade.fines().collectFine(session, id);
                refresh(session);
                AppTheme.success(this, "Payment collected successfully!\nReceipt generated for Fine ID: " + id);
            } catch (Exception ex) {
                AppTheme.error(this, ex.getMessage());
            }
        }
    }
}
