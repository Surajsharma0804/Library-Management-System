package com.library.gui;

import com.library.util.Page;
import com.library.util.Paginator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Generic paginated, filterable, sortable table component.
 * Wraps a {@link JTable} with prev/next navigation controls and integrates
 * with {@link com.library.util.Paginator} for stateless page slicing.
 *
 * <p>Usage:
 * <pre>
 *     PaginatedTable&lt;Book&gt; pt = new PaginatedTable&lt;&gt;(
 *         new String[]{"ID", "Title", "Author"},
 *         b -> new Object[]{b.getId(), b.getTitle(), b.getAuthor()},
 *         20
 *     );
 *     pt.load(books);
 * </pre>
 *
 * @param <T> the item type displayed in each row
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class PaginatedTable<T> extends JPanel {

    // ── Configuration ──────────────────────────────────────────────────────────
    private final String[]          columns;
    private final Function<T, Object[]> rowMapper;
    private final int               defaultPageSize;

    // ── Data state ─────────────────────────────────────────────────────────────
    private List<T> allData  = new ArrayList<>();
    private List<T> filtered = new ArrayList<>();
    private List<T> sorted   = new ArrayList<>();
    private int     currentPage = 1;

    private Predicate<T>  currentFilter = null;
    private Comparator<T> currentSort   = null;

    // ── Swing components ───────────────────────────────────────────────────────
    private final DefaultTableModel tableModel;
    private final JTable            table;
    private final JButton           prevBtn;
    private final JButton           nextBtn;
    private final JLabel            pageLabel;

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Constructs a {@code PaginatedTable}.
     *
     * @param columns         column header names
     * @param rowMapper       function mapping an item to a row's {@code Object[]}
     * @param defaultPageSize rows per page (must be between 1 and 500 inclusive)
     */
    public PaginatedTable(String[] columns, Function<T, Object[]> rowMapper, int defaultPageSize) {
        this.columns         = columns;
        this.rowMapper       = rowMapper;
        this.defaultPageSize = defaultPageSize;

        // ── Table model (non-editable) ─────────────────────────────────────────
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // ── JTable ────────────────────────────────────────────────────────────
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AppTheme.styleTable(table);

        // ── Pagination controls ───────────────────────────────────────────────
        prevBtn   = AppTheme.secondaryBtn("‹ Prev");
        nextBtn   = AppTheme.secondaryBtn("Next ›");
        pageLabel = AppTheme.label("Page 1 of 1");
        pageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        prevBtn.setPreferredSize(new Dimension(90, 34));
        nextBtn.setPreferredSize(new Dimension(90, 34));

        prevBtn.addActionListener(e -> goToPage(currentPage - 1));
        nextBtn.addActionListener(e -> goToPage(currentPage + 1));

        // ── Navigation bar ────────────────────────────────────────────────────
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(4, 0, 4, 0));
        navBar.add(prevBtn);
        navBar.add(pageLabel);
        navBar.add(nextBtn);

        // ── Layout ────────────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        setOpaque(false);
        add(AppTheme.scroll(table), BorderLayout.CENTER);
        add(navBar, BorderLayout.SOUTH);

        // Initial render with empty data
        renderPage(1);
    }

    // ── Public API ──────────────────────────────────────────────────────────────

    /**
     * Replaces all data and resets to page 1. Applies any active filter/sort.
     *
     * @param data new full data set
     */
    public void load(List<T> data) {
        this.allData = new ArrayList<>(data);
        applyFiltersAndSort();
        goToPage(1);
    }

    /**
     * Sets a filter predicate and resets to page 1.
     * Pass {@code null} to clear the filter.
     *
     * @param filter predicate to include an item, or {@code null} to show all
     */
    public void setFilter(Predicate<T> filter) {
        this.currentFilter = filter;
        applyFiltersAndSort();
        goToPage(1);
    }

    /**
     * Sets a sort comparator and resets to page 1.
     * Pass {@code null} to clear the sort.
     *
     * @param sort comparator for ordering items, or {@code null} for natural order
     */
    public void setSort(Comparator<T> sort) {
        this.currentSort = sort;
        applyFiltersAndSort();
        goToPage(1);
    }

    /**
     * Returns the item for the currently selected table row, or {@code null}
     * if no row is selected.
     *
     * @return selected item, or {@code null}
     */
    public T getSelectedItem() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Page<T> page = Paginator.paginate(sorted, currentPage, defaultPageSize);
        List<T> items = page.items();
        return row < items.size() ? items.get(row) : null;
    }

    /**
     * Exposes the underlying {@link JTable} for advanced customisation
     * (e.g., custom cell renderers, column width adjustments).
     *
     * @return the JTable managed by this component
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Returns the underlying {@link DefaultTableModel}.
     *
     * @return the table model
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    // ── Internal ────────────────────────────────────────────────────────────────

    /** Applies current filter and sort to {@code allData}, populating {@code sorted}. */
    private void applyFiltersAndSort() {
        Stream<T> stream = allData.stream();
        if (currentFilter != null) stream = stream.filter(currentFilter);
        if (currentSort   != null) stream = stream.sorted(currentSort);
        sorted   = stream.toList();
        filtered = sorted; // alias — kept for symmetry with spec
    }

    /** Renders the given page number into the table model and updates controls. */
    private void renderPage(int page) {
        Page<T> pageData = Paginator.paginate(sorted, page, defaultPageSize);
        tableModel.setRowCount(0);
        for (T item : pageData.items()) {
            tableModel.addRow(rowMapper.apply(item));
        }
        currentPage = page;
        pageLabel.setText("Page " + pageData.currentPage() + " of " + pageData.totalPages());
        prevBtn.setEnabled(pageData.hasPrevious());
        nextBtn.setEnabled(pageData.hasNext());
    }

    /**
     * Navigates to a page, clamping to valid bounds [1, totalPages].
     *
     * @param page requested page number (may be out of bounds; will be clamped)
     */
    private void goToPage(int page) {
        // Guard: sorted may be empty — Paginator returns totalPages=1 in that case
        Page<T> pageData = Paginator.paginate(sorted, Math.max(1, page), defaultPageSize);
        int safePage = Math.min(Math.max(1, page), pageData.totalPages());
        renderPage(safePage);
    }
}
