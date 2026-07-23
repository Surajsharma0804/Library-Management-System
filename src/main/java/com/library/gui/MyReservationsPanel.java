package com.library.gui;

import com.library.enums.ReservationStatus;
import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.Student;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Student's personal reservation panel — view and cancel own book reservations.
 * Columns: Book Title, Reserved Date, Status, Expiry Date.
 * READY rows are shown in bold accent colour.
 */
public final class MyReservationsPanel extends JPanel {

    private final LibraryFacade facade;
    private JTable table;
    private DefaultTableModel model;
    private Session session;

    // Keep full reservation list in sync with table rows for action access
    private List<Reservation> reservations = List.of();

    private static final String[] COLS = {"Book Title", "Reserved Date", "Status", "Expiry Date"};

    public MyReservationsPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("My Reservations"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Hold books and track your queue position"));

        JButton reserveBtn = AppTheme.primaryBtn("Reserve Book");
        reserveBtn.setPreferredSize(new Dimension(140, 40));
        reserveBtn.addActionListener(e -> reserve());

        JButton cancelBtn = AppTheme.dangerBtn("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.addActionListener(e -> cancel());

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(reserveBtn);
        acts.add(cancelBtn);
        hdr.add(title, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        AppTheme.styleTable(table);

        // Status column renderer — bold + accent for READY rows, colour-coded for others
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String status = value != null ? value.toString() : "";
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? AppTheme.bgCard() : AppTheme.tableAlt());
                } else {
                    comp.setBackground(new Color(
                            AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(), AppTheme.ACCENT.getBlue(), 40));
                }
                switch (status) {
                    case "READY" -> {
                        comp.setForeground(AppTheme.ACCENT);
                        comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                    }
                    case "PENDING" -> {
                        comp.setForeground(AppTheme.AMBER);
                        comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
                    }
                    case "CANCELLED", "EXPIRED" -> {
                        comp.setForeground(AppTheme.RED);
                        comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
                    }
                    default -> {
                        comp.setForeground(AppTheme.fg());
                        comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return comp;
            }
        });

        // Zebra + selection for non-status columns
        javax.swing.table.TableCellRenderer baseRenderer = (tbl, value, isSelected, hasFocus, row, col) -> {
            JLabel lbl = new JLabel(value != null ? value.toString() : "");
            lbl.setFont(AppTheme.BODY);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            lbl.setOpaque(true);
            if (!isSelected) {
                lbl.setBackground(row % 2 == 0 ? AppTheme.bgCard() : AppTheme.tableAlt());
            } else {
                lbl.setBackground(new Color(
                        AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(), AppTheme.ACCENT.getBlue(), 40));
            }
            lbl.setForeground(AppTheme.fg());
            return lbl;
        };
        table.getColumnModel().getColumn(0).setCellRenderer(baseRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(baseRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(baseRenderer);

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false);
        tbl.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH);
        add(tbl, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        model.setRowCount(0);
        reservations = List.of();

        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() {
                Student student = facade.userRepo().findStudentByUsername(s.username());
                if (student == null) return List.of();
                return facade.reservations().findByStudent(student.getRegistrationNumber());
            }

            @Override
            protected void done() {
                try {
                    reservations = get();
                    model.setRowCount(0);
                    for (Reservation r : reservations) {
                        String bookTitle = resolveBookTitle(r.getBookId());
                        model.addRow(new Object[]{
                                bookTitle,
                                r.getReservationDate() != null ? r.getReservationDate().toString() : "-",
                                r.getStatus().name(),
                                r.getExpiryDate() != null ? r.getExpiryDate().toString() : "-"
                        });
                    }
                } catch (Exception e) {
                    AppTheme.error(MyReservationsPanel.this, "Failed to load reservations: " + e.getMessage());
                }
                revalidate(); repaint();
            }
        }.execute();
    }

    private String resolveBookTitle(String bookId) {
        try {
            Optional<Book> book = facade.bookRepo().findById(bookId);
            return book.map(Book::getTitle).orElse(bookId);
        } catch (Exception e) {
            return bookId;
        }
    }

    private void reserve() {
        if (session == null) return;
        Student student = facade.userRepo().findStudentByUsername(session.username());
        if (student == null) { AppTheme.error(this, "Student profile not found."); return; }
        String bookId = JOptionPane.showInputDialog(this, "Enter Book ID to reserve:");
        if (bookId == null || bookId.trim().isEmpty()) return;
        try {
            Reservation r = facade.reservations().reserve(session, bookId.trim(), student.getRegistrationNumber());
            refresh(session);
            AppTheme.success(this, "Reserved!\nQueue position: #" + r.getQueuePosition());
        } catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
    }

    private void cancel() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= reservations.size()) {
            AppTheme.error(this, "Select a reservation to cancel.");
            return;
        }
        Reservation r = reservations.get(row);

        // Verify ownership
        Student student = facade.userRepo().findStudentByUsername(session.username());
        if (student == null || !r.getRegistrationNumber().equals(student.getRegistrationNumber())) {
            AppTheme.error(this, "You can only cancel your own reservations.");
            return;
        }

        if (AppTheme.confirm(this, "Cancel reservation for '" + resolveBookTitle(r.getBookId()) + "'?")) {
            try {
                facade.reservations().cancel(session, r.getId());
                refresh(session);
                AppTheme.success(this, "Reservation cancelled.");
            } catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
        }
    }
}
