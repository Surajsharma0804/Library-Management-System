package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Book management panel with searchable table and CRUD operations.
 */
public final class BooksPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private Session session;

    private static final String[] COLUMNS = {"ID", "Title", "Author", "ISBN", "Category", "Available", "Total", "Status"};

    public BooksPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bgPrimary());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JLabel title = AppTheme.heading("Books");
        JLabel subtitle = AppTheme.secondaryLabel("Manage the library book inventory");

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        // Search bar
        searchField = AppTheme.styledTextField(20);
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.setMaximumSize(new Dimension(300, 40));
        searchField.putClientProperty("JTextField.placeholderText", "Search books...");
        searchField.addActionListener(e -> filterBooks());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterBooks(); }
        });

        // Buttons
        JButton addBtn = AppTheme.primaryButton("+ Add Book");
        addBtn.setPreferredSize(new Dimension(130, 40));
        addBtn.addActionListener(e -> showAddDialog());

        JButton refreshBtn = AppTheme.secondaryButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 40));
        refreshBtn.addActionListener(e -> refresh(session));

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(searchField);
        actionsPanel.add(addBtn);
        actionsPanel.add(refreshBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? AppTheme.bgSecondary() : AppTheme.tableRowAlt());
                } else {
                    c.setBackground(AppTheme.ACCENT_DARK);
                }
                c.setForeground(AppTheme.textPrimary());
                // Color the status column
                if (col == 7) {
                    String val = String.valueOf(getValueAt(row, col));
                    if ("AVAILABLE".equals(val)) c.setForeground(AppTheme.SUCCESS);
                    else if ("BORROWED".equals(val)) c.setForeground(AppTheme.WARNING);
                    else c.setForeground(AppTheme.DANGER);
                }
                return c;
            }
        };
        AppTheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        // Context actions on double click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    showBookDetails(table.getSelectedRow());
                }
            }
        });

        JScrollPane sp = AppTheme.styledScrollPane(table);

        // Footer with count
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        add(header, BorderLayout.NORTH);
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        tableContainer.add(sp, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        this.session = session;
        tableModel.setRowCount(0);
        List<Book> books = facade.bookRepo().findAll();
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getCategory() != null ? b.getCategory() : "-",
                    b.getAvailableQuantity(), b.getTotalQuantity(),
                    b.getStatus().name()
            });
        }
    }

    private void filterBooks() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        List<Book> books = facade.bookRepo().findAll();
        for (Book b : books) {
            if (query.isEmpty()
                    || b.getTitle().toLowerCase().contains(query)
                    || b.getAuthor().toLowerCase().contains(query)
                    || b.getIsbn().toLowerCase().contains(query)
                    || (b.getCategory() != null && b.getCategory().toLowerCase().contains(query))) {
                tableModel.addRow(new Object[]{
                        b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                        b.getCategory() != null ? b.getCategory() : "-",
                        b.getAvailableQuantity(), b.getTotalQuantity(),
                        b.getStatus().name()
                });
            }
        }
    }

    private void showAddDialog() {
        if (session == null) return;
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(AppTheme.bgSecondary());
        JTextField titleF = new JTextField(20); JTextField authorF = new JTextField(20);
        JTextField isbnF = new JTextField(20); JTextField qtyF = new JTextField("1");
        form.add(lbl("Title:")); form.add(titleF);
        form.add(lbl("Author:")); form.add(authorF);
        form.add(lbl("ISBN:")); form.add(isbnF);
        form.add(lbl("Quantity:")); form.add(qtyF);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int qty = Integer.parseInt(qtyF.getText().trim());
                facade.books().addBook(session, isbnF.getText().trim(),
                        titleF.getText().trim(), authorF.getText().trim(), qty);
                refresh(session);
                AppTheme.showSuccess(this, "Book added successfully!");
            } catch (Exception ex) {
                AppTheme.showError(this, ex.getMessage());
            }
        }
    }

    private void showBookDetails(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        String info = String.format(
                "ID: %s\nTitle: %s\nAuthor: %s\nISBN: %s\nCategory: %s\nAvailable: %s / %s\nStatus: %s",
                tableModel.getValueAt(row, 0), tableModel.getValueAt(row, 1),
                tableModel.getValueAt(row, 2), tableModel.getValueAt(row, 3),
                tableModel.getValueAt(row, 4), tableModel.getValueAt(row, 5),
                tableModel.getValueAt(row, 6), tableModel.getValueAt(row, 7));
        JOptionPane.showMessageDialog(this, info, "Book Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setFont(AppTheme.FONT_BODY); return l; }
}
