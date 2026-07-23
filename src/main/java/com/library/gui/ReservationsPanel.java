package com.library.gui;

import com.library.enums.ReservationStatus;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.Reservation;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Executive Reservations Panel — PaginatedTable with Book Title, Student Name,
 * status filter, date range, Mark Ready and Cancel actions.
 * Admin and Librarian only.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class ReservationsPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    private PaginatedTable<Reservation> table;
    private JComboBox<String> statusFilter;
    private JTextField fromField, toField;

    private List<Reservation> allReservations = List.of();

    private static final String[] COLS = {
            "ID", "Book Title", "Student Name", "Reg No",
            "Status", "Reserved Date", "Expiry Date"
    };

    public ReservationsPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("Reservation Queue"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Manage hold requests, queue ordering, and expiration"));

        JButton markReadyBtn = AppTheme.primaryBtn("Mark Ready");
        JButton cancelBtn    = AppTheme.dangerBtn("Cancel Selected");
        JButton refBtn       = AppTheme.secondaryBtn("Refresh");

        markReadyBtn.setPreferredSize(new Dimension(120, 38));
        cancelBtn.setPreferredSize(new Dimension(140, 38));
        refBtn.setPreferredSize(new Dimension(100, 38));

        markReadyBtn.addActionListener(e -> doMarkReady());
        cancelBtn.addActionListener(e    -> doCancel());
        refBtn.addActionListener(e       -> refresh(session));

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(markReadyBtn);
        acts.add(cancelBtn);
        acts.add(refBtn);

        hdr.add(title, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        // ── Filter bar ────────────────────────────────────────────────────────
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterPanel.setOpaque(false);

        statusFilter = AppTheme.comboBox("All Statuses",
                ReservationStatus.PENDING.name(),
                ReservationStatus.READY.name(),
                ReservationStatus.FULFILLED.name(),
                ReservationStatus.CANCELLED.name(),
                ReservationStatus.EXPIRED.name());
        statusFilter.setPreferredSize(new Dimension(160, 36));

        fromField = AppTheme.textField(10);
        fromField.putClientProperty("JTextField.placeholderText", "From (yyyy-MM-dd)");
        fromField.setPreferredSize(new Dimension(130, 36));

        toField = AppTheme.textField(10);
        toField.putClientProperty("JTextField.placeholderText", "To (yyyy-MM-dd)");
        toField.setPreferredSize(new Dimension(130, 36));

        JButton applyBtn = AppTheme.primaryBtn("Apply");
        applyBtn.setPreferredSize(new Dimension(90, 36));
        applyBtn.addActionListener(e -> applyFilter());

        JButton clearBtn = AppTheme.secondaryBtn("Clear");
        clearBtn.setPreferredSize(new Dimension(80, 36));
        clearBtn.addActionListener(e -> {
            statusFilter.setSelectedIndex(0);
            fromField.setText("");
            toField.setText("");
            applyFilter();
        });

        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(fromField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toField);
        filterPanel.add(applyBtn);
        filterPanel.add(clearBtn);

        // ── Paginated Table ───────────────────────────────────────────────────
        table = new PaginatedTable<>(COLS, this::toRow, 25);

        // Status pill renderer — column 4
        table.getTable().getColumnModel().getColumn(4).setCellRenderer(
                (tbl, val, isSelected, hasFocus, row, col) -> {
                    JPanel pill = AppTheme.createStatusPill(val != null ? val.toString() : "UNKNOWN");
                    if (isSelected) { pill.setOpaque(true); pill.setBackground(tbl.getSelectionBackground()); }
                    else { pill.setOpaque(false); }
                    return pill;
                });
        table.getTable().getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getTable().getColumnModel().getColumn(2).setPreferredWidth(150);

        // ── North composite ───────────────────────────────────────────────────
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(hdr, BorderLayout.NORTH);
        north.add(filterPanel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    private Object[] toRow(Reservation r) {
        // Resolve book title
        String bookTitle = r.getBookId();
        try {
            var book = facade.bookRepo().findById(r.getBookId());
            if (book.isPresent()) bookTitle = book.get().getTitle();
        } catch (Exception ignored) {}

        // Resolve student name
        String studentName = r.getRegistrationNumber();
        try {
            var student = facade.userRepo().findStudentByRegistrationNumber(r.getRegistrationNumber());
            if (student != null) studentName = student.fullName();
        } catch (Exception ignored) {}

        return new Object[]{
                r.getId(),
                bookTitle,
                studentName,
                r.getRegistrationNumber(),
                r.getStatus().name(),
                r.getReservationDate() != null ? r.getReservationDate().toString() : "-",
                r.getExpiryDate() != null ? r.getExpiryDate().toString() : "-"
        };
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());

        // STUDENT role cannot access this panel
        if (s != null && s.role() == UserRole.STUDENT) {
            removeAll();
            setLayout(new BorderLayout());
            JLabel denied = new JLabel("Access restricted — Reservations management is for Admin and Librarian only.");
            denied.setFont(AppTheme.H3); denied.setForeground(AppTheme.RED);
            denied.setHorizontalAlignment(SwingConstants.CENTER);
            add(denied, BorderLayout.CENTER);
            revalidate(); repaint();
            return;
        }

        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() {
                return facade.reservations().findAll();
            }
            @Override
            protected void done() {
                try {
                    allReservations = get();
                    applyFilter();
                } catch (Exception ex) {
                    AppTheme.error(ReservationsPanel.this, "Failed to load reservations: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String statusSel = (String) statusFilter.getSelectedItem();
        String fromStr   = fromField.getText().trim();
        String toStr     = toField.getText().trim();

        boolean filterStatus = statusSel != null && !statusSel.equals("All Statuses");
        LocalDate fromDate = null, toDate = null;
        try { if (!fromStr.isEmpty()) fromDate = LocalDate.parse(fromStr); } catch (Exception ignored) {}
        try { if (!toStr.isEmpty())   toDate   = LocalDate.parse(toStr);   } catch (Exception ignored) {}

        final LocalDate fd = fromDate;
        final LocalDate td = toDate;

        table.setFilter(r -> {
            if (filterStatus && !r.getStatus().name().equals(statusSel)) return false;
            if (fd != null && r.getReservationDate() != null && r.getReservationDate().isBefore(fd)) return false;
            if (td != null && r.getReservationDate() != null && r.getReservationDate().isAfter(td)) return false;
            return true;
        });
        table.load(allReservations);
    }

    private void doMarkReady() {
        try {
            facade.rbac().require(session, Permissions.RESERVATION_VIEW_ALL);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        Reservation sel = table.getSelectedItem();
        if (sel == null) {
            AppTheme.error(this, "Select a reservation from the table first.");
            return;
        }
        if (sel.getStatus() != ReservationStatus.PENDING) {
            AppTheme.error(this, "Only PENDING reservations can be marked ready.");
            return;
        }
        if (!AppTheme.confirm(this, "Mark reservation " + sel.getId() + " as READY for pickup?")) return;

        try {
            facade.reservations().markReady(session, sel.getId());
            refresh(session);
            AppTheme.success(this, "Reservation marked as READY.");
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
        }
    }

    private void doCancel() {
        try {
            facade.rbac().require(session, Permissions.RESERVATION_CANCEL);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        Reservation sel = table.getSelectedItem();
        if (sel == null) {
            AppTheme.error(this, "Select a reservation from the table first.");
            return;
        }
        if (!AppTheme.confirm(this, "Cancel reservation " + sel.getId() + "?")) return;

        try {
            facade.reservations().cancel(session, sel.getId());
            refresh(session);
            AppTheme.success(this, "Reservation cancelled successfully.");
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
        }
    }
}
