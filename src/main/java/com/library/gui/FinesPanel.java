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
 * Fines management — view and collect overdue fines.
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
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("Fines"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Track and collect overdue fines"));

        JButton payBtn = AppTheme.primaryBtn("Collect Fine");
        payBtn.setPreferredSize(new Dimension(130, 40));
        payBtn.addActionListener(e -> collectFine());

        JButton refBtn = AppTheme.secondaryBtn("Refresh");
        refBtn.setPreferredSize(new Dimension(100, 40));
        refBtn.addActionListener(e -> refresh(session));

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(payBtn); acts.add(refBtn);
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
                    if ("PAID".equals(v)) comp.setForeground(AppTheme.GREEN);
                    else comp.setForeground(AppTheme.RED);
                }
                if (c == 3) comp.setForeground(AppTheme.AMBER);
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
        List<Fine> fines = facade.fineRepo().findAll();
        fines.stream()
                .sorted((a, b) -> { boolean ap = a.getStatus()==FineStatus.PAID, bp = b.getStatus()==FineStatus.PAID;
                    return ap != bp ? (ap ? 1 : -1) : 0; })
                .forEach(f -> model.addRow(new Object[]{
                        f.getId(), f.getRegistrationNumber(), f.getBookId(),
                        String.format("\u20B9%.2f", f.getAmountPaise()/100.0), f.getReason(),
                        f.getStatus()==FineStatus.PAID ? "PAID" : "UNPAID",
                        f.getCreatedAt()!=null ? f.getCreatedAt().toLocalDate().toString() : "-"}));
    }

    private void collectFine() {
        if (session == null) return;
        int row = table.getSelectedRow();
        if (row < 0) { AppTheme.error(this, "Select a fine to collect."); return; }
        String id = (String) model.getValueAt(row, 0);
        String st = (String) model.getValueAt(row, 5);
        if ("PAID".equals(st)) { AppTheme.error(this, "Already paid."); return; }
        String amt = (String) model.getValueAt(row, 3);
        if (AppTheme.confirm(this, "Collect fine " + id + " (" + amt + ")?")) {
            try { facade.fines().collectFine(session, id); refresh(session);
                AppTheme.success(this, "Fine collected!"); }
            catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
        }
    }
}
