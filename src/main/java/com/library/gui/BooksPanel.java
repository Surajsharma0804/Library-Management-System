package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Book catalogue management — search, add, view.
 */
public final class BooksPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private Session session;

    private static final String[] COLS = {"ID", "Title", "Author", "ISBN", "Category", "Available", "Total", "Status"};

    public BooksPanel(LibraryFacade facade) {
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
        title.add(AppTheme.heading("Book Catalogue"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Browse and manage the library collection"));

        searchField = AppTheme.textField(22);
        searchField.putClientProperty("JTextField.placeholderText", "Search books...");
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JButton addBtn = AppTheme.primaryBtn("+ Add Book");
        addBtn.setPreferredSize(new Dimension(130, 40));
        addBtn.addActionListener(e -> addBook());

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acts.setOpaque(false);
        acts.add(searchField); acts.add(addBtn);
        hdr.add(title, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);

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

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false);
        tbl.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH); add(tbl, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        load(facade.bookRepo().findAll());
    }

    private void load(List<Book> books) {
        model.setRowCount(0);
        for (Book b : books) model.addRow(new Object[]{
                b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                b.getCategory() != null ? b.getCategory() : "-",
                b.getAvailableQuantity(), b.getTotalQuantity(),
                b.getStatus().name()});
    }

    private void filter() {
        String q = searchField.getText().trim().toLowerCase();
        List<Book> all = facade.bookRepo().findAll();
        if (q.isEmpty()) { load(all); return; }
        load(all.stream().filter(b ->
                b.getTitle().toLowerCase().contains(q) ||
                b.getAuthor().toLowerCase().contains(q) ||
                (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(q)) ||
                (b.getCategory() != null && b.getCategory().toLowerCase().contains(q))
        ).toList());
    }

    private void addBook() {
        if (session == null) return;
        JPanel f = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField ti = new JTextField(), au = new JTextField(), is = new JTextField();
        JTextField pu = new JTextField(), ca = new JTextField(), qt = new JTextField("1");
        f.add(lbl("Title:")); f.add(ti);
        f.add(lbl("Author:")); f.add(au);
        f.add(lbl("ISBN:")); f.add(is);
        f.add(lbl("Publisher:")); f.add(pu);
        f.add(lbl("Category:")); f.add(ca);
        f.add(lbl("Quantity:")); f.add(qt);
        if (JOptionPane.showConfirmDialog(this, f, "Add New Book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Book b = facade.factory().createBook(is.getText().trim(), ti.getText().trim(),
                        au.getText().trim(), Integer.parseInt(qt.getText().trim()));
                b.setPublisher(pu.getText().trim());
                b.setCategory(ca.getText().trim());
                facade.bookRepo().save(b);
                refresh(session);
                AppTheme.success(this, "Book added!\nID: " + b.getId());
            } catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
        }
    }

    private JLabel lbl(String t) { var l = new JLabel(t); l.setFont(AppTheme.BODY); return l; }
}
