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
 * Fines management panel with payment actions.
 */
public final class FinesPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel tableModel;
    private Session session;

    private static final String[] COLUMNS = {"Fine ID", "Student Reg", "Book ID", "Amount", "Reason", "Status", "Date"};

    public FinesPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bgPrimary());
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
        titlePanel.add(AppTheme.heading("Fines"));
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(AppTheme.secondaryLabel("Track and manage overdue fines"));

        JButton payBtn = AppTheme.primaryButton("Collect Fine");
        payBtn.setPreferredSize(new Dimension(130, 40));
        payBtn.addActionListener(e -> paySelectedFine());

        JButton refreshBtn = AppTheme.secondaryButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 40));
        refreshBtn.addActionListener(e -> refresh(session));

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(payBtn);
        actionsPanel.add(refreshBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? AppTheme.bgSecondary() : AppTheme.tableRowAlt());
                } else { c.setBackground(AppTheme.ACCENT_DARK); }
                c.setForeground(AppTheme.textPrimary());
                if (col == 5) {
                    String val = String.valueOf(getValueAt(row, col));
                    if ("PAID".equals(val)) c.setForeground(AppTheme.SUCCESS);
                    else c.setForeground(AppTheme.DANGER);
                }
                if (col == 3) c.setForeground(AppTheme.WARNING);
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
        List<Fine> fines = facade.fineRepo().findAll();
        fines.stream()
                .sorted((a, b) -> {
                    boolean aPaid = a.getStatus() == FineStatus.PAID;
                    boolean bPaid = b.getStatus() == FineStatus.PAID;
                    if (aPaid != bPaid) return aPaid ? 1 : -1;
                    return 0;
                })
                .forEach(f -> tableModel.addRow(new Object[]{
                        f.getId(), f.getRegistrationNumber(), f.getBookId(),
                        String.format("\u20B9%.2f", f.getAmountPaise() / 100.0),
                        f.getReason(),
                        f.getStatus() == FineStatus.PAID ? "PAID" : "UNPAID",
                        f.getCreatedAt() != null ? f.getCreatedAt().toLocalDate().toString() : "-"
                }));
    }

    private void paySelectedFine() {
        if (session == null) return;
        int row = table.getSelectedRow();
        if (row < 0) {
            AppTheme.showError(this, "Please select a fine to collect.");
            return;
        }
        String fineId = (String) tableModel.getValueAt(row, 0);
        String amount = (String) tableModel.getValueAt(row, 3);
        String status = (String) tableModel.getValueAt(row, 5);

        if ("PAID".equals(status)) {
            AppTheme.showError(this, "This fine is already paid.");
            return;
        }

        if (AppTheme.confirm(this, "Collect fine " + fineId + " (" + amount + ")?")) {
            try {
                facade.fines().collectFine(session, fineId);
                refresh(session);
                AppTheme.showSuccess(this, "Fine collected successfully!");
            } catch (Exception ex) {
                AppTheme.showError(this, ex.getMessage());
            }
        }
    }
}
