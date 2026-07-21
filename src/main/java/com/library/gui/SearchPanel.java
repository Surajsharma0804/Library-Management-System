package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Advanced search panel with multi-criteria filtering.
 */
public final class SearchPanel extends JPanel {

    private final LibraryFacade facade;
    private JTextField titleField, authorField, isbnField, categoryField;
    private JTable table;
    private DefaultTableModel model;
    private Session session;

    private static final String[] COLS = {"ID", "Title", "Author", "ISBN", "Category", "Publisher", "Available", "Status"};

    public SearchPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel();
        hdr.setOpaque(false); hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(AppTheme.heading("Advanced Search"));
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(AppTheme.label2("Search books by multiple criteria"));

        // filter form
        JPanel form = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (!AppTheme.isDark()) {
                    g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                }
            }
        };
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        var gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(0, 6, 0, 6);
        gbc.weightx = 1;

        titleField    = AppTheme.textField(15); titleField.setPreferredSize(new Dimension(0, 38));
        authorField   = AppTheme.textField(15); authorField.setPreferredSize(new Dimension(0, 38));
        isbnField     = AppTheme.textField(12); isbnField.setPreferredSize(new Dimension(0, 38));
        categoryField = AppTheme.textField(12); categoryField.setPreferredSize(new Dimension(0, 38));

        gbc.gridx = 0; form.add(col("Title", titleField), gbc);
        gbc.gridx = 1; form.add(col("Author", authorField), gbc);
        gbc.gridx = 2; form.add(col("ISBN", isbnField), gbc);
        gbc.gridx = 3; form.add(col("Category", categoryField), gbc);
        gbc.gridx = 4; gbc.weightx = 0;
        JButton go = AppTheme.primaryBtn("Search");
        go.setPreferredSize(new Dimension(100, 38));
        go.addActionListener(e -> search());
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        btnWrap.add(go, BorderLayout.CENTER);
        form.add(btnWrap, gbc);

        titleField.addActionListener(e -> search());
        authorField.addActionListener(e -> search());
        isbnField.addActionListener(e -> search());
        categoryField.addActionListener(e -> search());

        // table
        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rn, int r, int c) {
                Component comp = super.prepareRenderer(rn, r, c);
                if (!isRowSelected(r)) comp.setBackground(r % 2 == 0 ? AppTheme.bgCard() : AppTheme.tableAlt());
                else comp.setBackground(new Color(AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(), AppTheme.ACCENT.getBlue(), 40));
                comp.setForeground(AppTheme.fg());
                if (c == 7) { String v = String.valueOf(getValueAt(r, c));
                    if ("AVAILABLE".equals(v)) comp.setForeground(AppTheme.GREEN);
                    else comp.setForeground(AppTheme.RED);
                }
                return comp;
            }
        };
        AppTheme.styleTable(table);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        formWrap.add(form, BorderLayout.NORTH);

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false); tbl.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        body.add(formWrap, BorderLayout.NORTH);
        body.add(tbl, BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH); add(body, BorderLayout.CENTER);
    }

    private JPanel col(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(AppTheme.SMALL_B); l.setForeground(AppTheme.fgSecondary());
        p.add(l, BorderLayout.NORTH); p.add(field, BorderLayout.CENTER);
        return p;
    }

    public void refresh(Session s) { this.session = s; setBackground(AppTheme.bg()); }

    private void search() {
        model.setRowCount(0);
        String t = titleField.getText().trim().toLowerCase();
        String a = authorField.getText().trim().toLowerCase();
        String i = isbnField.getText().trim().toLowerCase();
        String c = categoryField.getText().trim().toLowerCase();
        List<Book> results = facade.bookRepo().findAll().stream().filter(b -> {
            boolean ok = true;
            if (!t.isEmpty()) ok = b.getTitle().toLowerCase().contains(t);
            if (!a.isEmpty()) ok = ok && b.getAuthor().toLowerCase().contains(a);
            if (!i.isEmpty()) ok = ok && b.getIsbn() != null && b.getIsbn().toLowerCase().contains(i);
            if (!c.isEmpty()) ok = ok && b.getCategory() != null && b.getCategory().toLowerCase().contains(c);
            return ok;
        }).toList();

        for (Book b : results) model.addRow(new Object[]{
                b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                b.getCategory() != null ? b.getCategory() : "-",
                b.getPublisher() != null ? b.getPublisher() : "-",
                b.getAvailableQuantity(), b.getStatus().name()});

        if (results.isEmpty()) AppTheme.error(this, "No books found matching the criteria.");
    }
}
