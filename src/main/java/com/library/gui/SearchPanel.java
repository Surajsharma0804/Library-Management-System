package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Advanced search panel with multiple filter criteria.
 */
public final class SearchPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField titleField, authorField, isbnField, categoryField;
    private Session session;

    private static final String[] COLUMNS = {"ID", "Title", "Author", "ISBN", "Category", "Available", "Total", "Status"};

    public SearchPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bgPrimary());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        buildUI();
    }

    private void buildUI() {
        // Title
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(AppTheme.heading("Search"));
        header.add(Box.createVerticalStrut(4));
        header.add(AppTheme.secondaryLabel("Find books by title, author, ISBN, or category"));
        header.add(Box.createVerticalStrut(20));

        // Search form card
        JPanel filterCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.CARD_ARC, AppTheme.CARD_ARC));
            }
        };
        filterCard.setOpaque(false);
        filterCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));

        JPanel fieldsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        fieldsRow.setOpaque(false);
        fieldsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        fieldsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleField = createFilterField("Title");
        authorField = createFilterField("Author");
        isbnField = createFilterField("ISBN");
        categoryField = createFilterField("Category");

        fieldsRow.add(fieldGroup("Title", titleField));
        fieldsRow.add(fieldGroup("Author", authorField));
        fieldsRow.add(fieldGroup("ISBN", isbnField));
        fieldsRow.add(fieldGroup("Category", categoryField));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton searchBtn = AppTheme.primaryButton("Search");
        searchBtn.setPreferredSize(new Dimension(120, 38));
        searchBtn.addActionListener(e -> doSearch());

        JButton clearBtn = AppTheme.secondaryButton("Clear");
        clearBtn.setPreferredSize(new Dimension(100, 38));
        clearBtn.addActionListener(e -> {
            titleField.setText(""); authorField.setText(""); isbnField.setText(""); categoryField.setText("");
            tableModel.setRowCount(0);
        });

        btnRow.add(searchBtn);
        btnRow.add(clearBtn);

        filterCard.add(fieldsRow);
        filterCard.add(btnRow);

        // Top section
        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.add(header);
        topSection.add(filterCard);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? AppTheme.bgSecondary() : AppTheme.tableRowAlt());
                } else { c.setBackground(AppTheme.ACCENT_DARK); }
                c.setForeground(AppTheme.textPrimary());
                if (col == 7) {
                    String val = String.valueOf(getValueAt(row, col));
                    if ("AVAILABLE".equals(val)) c.setForeground(AppTheme.SUCCESS);
                    else c.setForeground(AppTheme.WARNING);
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

        add(topSection, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    private void doSearch() {
        String t = titleField.getText().trim().toLowerCase();
        String a = authorField.getText().trim().toLowerCase();
        String i = isbnField.getText().trim().toLowerCase();
        String c = categoryField.getText().trim().toLowerCase();

        tableModel.setRowCount(0);
        List<Book> books = facade.bookRepo().findAll();
        for (Book b : books) {
            boolean match = true;
            if (!t.isEmpty() && !b.getTitle().toLowerCase().contains(t)) match = false;
            if (!a.isEmpty() && !b.getAuthor().toLowerCase().contains(a)) match = false;
            if (!i.isEmpty() && !b.getIsbn().toLowerCase().contains(i)) match = false;
            if (!c.isEmpty() && (b.getCategory() == null || !b.getCategory().toLowerCase().contains(c))) match = false;

            if (match) {
                tableModel.addRow(new Object[]{
                        b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                        b.getCategory() != null ? b.getCategory() : "-",
                        b.getAvailableQuantity(), b.getTotalQuantity(),
                        b.getStatus().name()
                });
            }
        }
    }

    public void refresh(Session session) {
        this.session = session;
    }

    private JTextField createFilterField(String placeholder) {
        JTextField f = AppTheme.styledTextField(15);
        f.setPreferredSize(new Dimension(180, 36));
        f.addActionListener(e -> doSearch());
        return f;
    }

    private JPanel fieldGroup(String label, JTextField field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.textSecondary());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        return p;
    }
}
