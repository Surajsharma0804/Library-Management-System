package com.library.gui;

import com.library.enums.FineStatus;
import com.library.facade.LibraryFacade;
import com.library.model.Fine;
import com.library.model.Student;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Student-specific fines view — shows only the logged-in
 * student's pending and paid fines.
 *
 * @author University Central Library — Software Engineering Division
 */
public final class MyFinesPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel model;
    private JLabel balanceLbl;
    private Session session;

    private static final String[] COLS = {"Fine ID", "Book ID", "Amount", "Reason", "Status", "Date"};

    public MyFinesPanel(LibraryFacade facade) {
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
        title.add(AppTheme.heading("My Fines"));
        title.add(Box.createVerticalStrut(4));
        balanceLbl = AppTheme.label2("Outstanding balance: \u20B90.00");
        title.add(balanceLbl);

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
                    if ("PAID".equals(v)) comp.setForeground(AppTheme.GREEN);
                    else comp.setForeground(AppTheme.RED);
                }
                if (c == 2) comp.setForeground(AppTheme.AMBER);
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

        long balance = student.getFineBalancePaise();
        balanceLbl.setText(String.format("Outstanding balance: \u20B9%.2f", balance / 100.0));
        if (balance > 0) balanceLbl.setForeground(AppTheme.RED);
        else balanceLbl.setForeground(AppTheme.GREEN);

        List<Fine> fines = facade.fineRepo().findByRegistrationNumber(student.getRegistrationNumber());
        fines.stream()
                .sorted((a, b) -> {
                    boolean ap = a.getStatus() == FineStatus.PAID, bp = b.getStatus() == FineStatus.PAID;
                    return ap != bp ? (ap ? 1 : -1) : 0;
                })
                .forEach(f -> model.addRow(new Object[]{
                        f.getId(), f.getBookId(),
                        String.format("\u20B9%.2f", f.getAmountPaise() / 100.0),
                        f.getReason(),
                        f.getStatus() == FineStatus.PAID ? "PAID" : "UNPAID",
                        f.getCreatedAt() != null ? f.getCreatedAt().toLocalDate().toString() : "-"}));
    }
}
