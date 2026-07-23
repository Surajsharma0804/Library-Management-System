package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executive Search Panel — Multi-criteria catalogue query engine with instant
 * result table and status pill renderers.
 * Filters: Title, Author, ISBN, Category, Dewey Decimal prefix, Language,
 * Year From/To, Availability (All/Available/Borrowed/Reserved).
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class SearchPanel extends JPanel {

    private final LibraryFacade facade;

    // Original filters
    private JTextField titleField, authorField, isbnField, categoryField;

    // New filters (Task 61.1)
    private JTextField deweyField;
    private JComboBox<String> languageCombo;
    private JTextField yearFromField, yearToField;
    private JComboBox<String> availabilityCombo;

    private JTable table;
    private DefaultTableModel model;
    private Session session;

    private static final String[] COLS = {
        "ID", "Title", "Author", "ISBN", "Category", "Publisher", "Available", "Status"
    };

    public SearchPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        removeAll();

        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(AppTheme.heading("Advanced Catalogue Search"));
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(AppTheme.label2("Execute targeted queries across titles, authors, ISBNs, categories, Dewey, language, year, and status"));

        // ── Filter form ──────────────────────────────────────────────
        JPanel form = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.CARD_R, AppTheme.CARD_R);
                g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.CARD_R, AppTheme.CARD_R);
            }
        };
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        var gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 8, 5);
        gbc.weightx = 1;

        // ── Row 1: Title, Author, ISBN, Category ─────────────────────
        titleField    = AppTheme.textField(12);
        titleField.putClientProperty("JTextField.placeholderText", "e.g. Algorithms");
        authorField   = AppTheme.textField(12);
        authorField.putClientProperty("JTextField.placeholderText", "e.g. Knuth");
        isbnField     = AppTheme.textField(10);
        isbnField.putClientProperty("JTextField.placeholderText", "e.g. 978-0…");
        categoryField = AppTheme.textField(10);
        categoryField.putClientProperty("JTextField.placeholderText", "e.g. Computer Science");

        gbc.gridy = 0;
        gbc.gridx = 0; form.add(col("Title Keyword",    titleField),    gbc);
        gbc.gridx = 1; form.add(col("Author Name",       authorField),   gbc);
        gbc.gridx = 2; form.add(col("ISBN Code",          isbnField),     gbc);
        gbc.gridx = 3; form.add(col("Subject Category",  categoryField), gbc);

        // ── Row 2: Dewey prefix, Language, Year From, Year To, Availability ──
        deweyField = AppTheme.textField(8);
        deweyField.putClientProperty("JTextField.placeholderText", "e.g. 005");

        // Language combo — populated with "All" + distinct book languages
        languageCombo = new JComboBox<>();
        languageCombo.setFont(AppTheme.BODY);
        languageCombo.addItem("All");
        populateLanguages();

        yearFromField = AppTheme.textField(6);
        yearFromField.putClientProperty("JTextField.placeholderText", "e.g. 2000");

        yearToField = AppTheme.textField(6);
        yearToField.putClientProperty("JTextField.placeholderText", "e.g. 2024");

        availabilityCombo = new JComboBox<>(new String[]{"All", "Available", "Borrowed", "Reserved"});
        availabilityCombo.setFont(AppTheme.BODY);

        gbc.gridy = 1;
        gbc.gridx = 0; form.add(col("Dewey Decimal",   deweyField),        gbc);
        gbc.gridx = 1; form.add(col("Language",         languageCombo),     gbc);
        gbc.gridx = 2; form.add(col("Year From",         yearFromField),     gbc);
        gbc.gridx = 3; form.add(col("Year To",           yearToField),       gbc);

        // ── Row 3: Availability + Search button ──────────────────────
        gbc.gridy = 2;
        gbc.gridx = 0; form.add(col("Availability",     availabilityCombo), gbc);

        JButton go = AppTheme.primaryBtn("Search");
        go.setPreferredSize(new Dimension(100, 38));
        go.addActionListener(e -> search());

        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        btnWrap.add(go, BorderLayout.SOUTH);

        gbc.gridx = 3; gbc.weightx = 0;
        form.add(btnWrap, gbc);

        // Enter-key triggers search in all fields
        for (JTextField f : new JTextField[]{
                titleField, authorField, isbnField, categoryField,
                deweyField, yearFromField, yearToField}) {
            f.addActionListener(e -> search());
        }

        // ── Results Table ────────────────────────────────────────────
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        AppTheme.styleTable(table);

        // Status column pill renderer (column index 7)
        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, isSelected, hasFocus, row, col) -> {
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

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false);
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        body.add(form, BorderLayout.NORTH);
        body.add(tbl, BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    /** Populate language combo with "All" + distinct non-null languages from the catalogue. */
    private void populateLanguages() {
        try {
            List<String> langs = facade.bookRepo().findAll().stream()
                    .map(Book::getLanguage)
                    .filter(l -> l != null && !l.isBlank())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            for (String l : langs) languageCombo.addItem(l);
        } catch (Exception ignored) {}
    }

    private JPanel col(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(AppTheme.SMALL_B);
        l.setForeground(AppTheme.fgSecondary());
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        // Refresh language list in case catalogue changed
        String currentLang = (String) languageCombo.getSelectedItem();
        languageCombo.removeAllItems();
        languageCombo.addItem("All");
        populateLanguages();
        if (currentLang != null) languageCombo.setSelectedItem(currentLang);
    }

    private void search() {
        model.setRowCount(0);

        String t     = titleField.getText().trim().toLowerCase();
        String a     = authorField.getText().trim().toLowerCase();
        String i     = isbnField.getText().trim().toLowerCase();
        String c     = categoryField.getText().trim().toLowerCase();
        String dewey = deweyField.getText().trim();
        String lang  = (String) languageCombo.getSelectedItem();
        boolean allLang = "All".equals(lang);
        String avail = (String) availabilityCombo.getSelectedItem();
        boolean allAvail = "All".equals(avail);

        int yearFrom = parseYear(yearFromField.getText().trim(), 0);
        int yearTo   = parseYear(yearToField.getText().trim(), Integer.MAX_VALUE);

        List<Book> results = facade.bookRepo().findAll().stream().filter(b -> {
            // Title / author / isbn / category
            if (!t.isEmpty() && !b.getTitle().toLowerCase().contains(t))            return false;
            if (!a.isEmpty() && !b.getAuthor().toLowerCase().contains(a))           return false;
            if (!i.isEmpty() && (b.getIsbn() == null
                    || !b.getIsbn().toLowerCase().contains(i)))                      return false;
            if (!c.isEmpty() && (b.getCategory() == null
                    || !b.getCategory().toLowerCase().contains(c)))                  return false;

            // Dewey Decimal prefix
            if (!dewey.isEmpty() && (b.getDeweyDecimal() == null
                    || !b.getDeweyDecimal().startsWith(dewey)))                      return false;

            // Language
            if (!allLang && (b.getLanguage() == null
                    || !b.getLanguage().equalsIgnoreCase(lang)))                     return false;

            // Publication year range
            int year = b.getPublicationYear();
            if (yearFrom > 0 && year < yearFrom)                                     return false;
            if (yearTo < Integer.MAX_VALUE && year > yearTo)                         return false;

            // Availability / status
            if (!allAvail && !b.getStatus().name().equalsIgnoreCase(avail))          return false;

            return true;
        }).toList();

        for (Book b : results) {
            model.addRow(new Object[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.getCategory() != null ? b.getCategory() : "-",
                    b.getPublisher() != null ? b.getPublisher() : "-",
                    b.getAvailableQuantity(),
                    b.getStatus().name()
            });
        }

        if (results.isEmpty()) {
            AppTheme.error(this, "No book records found matching the specified search criteria.");
        }
    }

    private int parseYear(String s, int fallback) {
        if (s == null || s.isBlank()) return fallback;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }
}
