package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.AuditLog;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Executive Audit Log Panel — Real-time security and action auditing with
 * PaginatedTable, date-range filters, actor search, and action-type filter.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class AuditLogPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    private PaginatedTable<AuditLog> table;
    private JTextField fromField;
    private JTextField toField;
    private JTextField actorField;
    private JComboBox<String> actionCombo;
    private JLabel countLabel;

    private List<AuditLog> allLogs = List.of();

    private static final String[] COLS = {
            "Timestamp", "Actor", "Role", "Action", "Entity Type", "Entity ID", "Details"
    };
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLogPanel(LibraryFacade facade) {
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

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(AppTheme.heading("System Audit Log"));
        titleBlock.add(Box.createVerticalStrut(4));
        countLabel = AppTheme.label2("System security event stream");
        titleBlock.add(countLabel);

        JButton refBtn = AppTheme.secondaryBtn("Refresh");
        refBtn.setPreferredSize(new Dimension(100, 38));
        refBtn.addActionListener(e -> refresh(session));

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        acts.add(refBtn);

        hdr.add(titleBlock, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        // ── Filter Panel ──────────────────────────────────────────────────────
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterPanel.setOpaque(false);

        fromField = AppTheme.textField(10);
        fromField.putClientProperty("JTextField.placeholderText", "From (yyyy-MM-dd)");
        fromField.setPreferredSize(new Dimension(130, 36));

        toField = AppTheme.textField(10);
        toField.putClientProperty("JTextField.placeholderText", "To (yyyy-MM-dd)");
        toField.setPreferredSize(new Dimension(130, 36));

        actorField = AppTheme.textField(12);
        actorField.putClientProperty("JTextField.placeholderText", "Actor username/ID...");
        actorField.setPreferredSize(new Dimension(160, 36));

        actionCombo = AppTheme.comboBox(
                "All Actions",
                "LOGIN", "LOGOUT",
                "BOOK_ADD", "BOOK_UPDATE", "BOOK_DELETE",
                "BORROW_ISSUE", "BORROW_RETURN", "BORROW_RENEW",
                "RESERVATION_CREATE", "RESERVATION_CANCEL",
                "FINE_WAIVE", "FINE_COLLECT",
                "STUDENT_ADD", "STUDENT_UPDATE",
                "LIBRARIAN_ADD", "LIBRARIAN_UPDATE", "LIBRARIAN_REMOVE",
                "BACKUP_CREATE", "BACKUP_RESTORE",
                "CONFIG_UPDATE", "MIGRATION_COMPLETE",
                "REPORT_EXPORT"
        );
        actionCombo.setPreferredSize(new Dimension(180, 36));

        JButton applyBtn = AppTheme.primaryBtn("Apply Filter");
        applyBtn.setPreferredSize(new Dimension(120, 36));
        applyBtn.addActionListener(e -> applyFilter());

        JButton clearBtn = AppTheme.secondaryBtn("Clear");
        clearBtn.setPreferredSize(new Dimension(80, 36));
        clearBtn.addActionListener(e -> {
            fromField.setText("");
            toField.setText("");
            actorField.setText("");
            actionCombo.setSelectedIndex(0);
            applyFilter();
        });

        filterPanel.add(new JLabel("From:"));
        filterPanel.add(fromField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toField);
        filterPanel.add(new JLabel("Actor:"));
        filterPanel.add(actorField);
        filterPanel.add(new JLabel("Action:"));
        filterPanel.add(actionCombo);
        filterPanel.add(applyBtn);
        filterPanel.add(clearBtn);

        // ── Paginated Table ───────────────────────────────────────────────────
        table = new PaginatedTable<>(COLS, this::toRow, 25);

        // Status pill renderer for Action column (index 3)
        table.getTable().getColumnModel().getColumn(3).setCellRenderer(
                (tbl, val, isSelected, hasFocus, row, col) -> {
                    JPanel pill = AppTheme.createStatusPill(val != null ? val.toString() : "UNKNOWN");
                    if (isSelected) { pill.setOpaque(true); pill.setBackground(tbl.getSelectionBackground()); }
                    else { pill.setOpaque(false); }
                    return pill;
                });
        table.getTable().getColumnModel().getColumn(6).setPreferredWidth(280);

        // ── North composite panel ─────────────────────────────────────────────
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(hdr, BorderLayout.NORTH);
        north.add(filterPanel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());

        // Check AUDIT_VIEW permission
        try {
            facade.rbac().require(session, Permissions.AUDIT_VIEW);
        } catch (Exception ex) {
            countLabel.setText("Access denied — AUDIT_VIEW permission required.");
            return;
        }

        new SwingWorker<List<AuditLog>, Void>() {
            @Override protected List<AuditLog> doInBackground() {
                return facade.audit().findAll();
            }
            @Override protected void done() {
                try {
                    allLogs = get().stream()
                            .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                            .toList();
                    applyFilter();
                } catch (Exception ex) {
                    countLabel.setText("Error loading audit logs: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String fromStr  = fromField.getText().trim();
        String toStr    = toField.getText().trim();
        String actor    = actorField.getText().trim().toLowerCase();
        String actionFilter = (String) actionCombo.getSelectedItem();

        LocalDate fromDate = null, toDate = null;
        try {
            if (!fromStr.isEmpty()) fromDate = LocalDate.parse(fromStr);
        } catch (Exception ignored) {}
        try {
            if (!toStr.isEmpty()) toDate = LocalDate.parse(toStr);
        } catch (Exception ignored) {}

        final LocalDate fd = fromDate;
        final LocalDate td = toDate;
        final boolean filterAction = actionFilter != null && !actionFilter.equals("All Actions");

        List<AuditLog> filtered = allLogs.stream().filter(log -> {
            if (fd != null && log.timestamp() != null
                    && log.timestamp().toLocalDate().isBefore(fd)) return false;
            if (td != null && log.timestamp() != null
                    && log.timestamp().toLocalDate().isAfter(td)) return false;
            if (!actor.isEmpty()) {
                String actorId = log.actorId() != null ? log.actorId().toLowerCase() : "";
                if (!actorId.contains(actor)) return false;
            }
            if (filterAction) {
                if (log.action() == null || !log.action().equalsIgnoreCase(actionFilter)) return false;
            }
            return true;
        }).toList();

        table.setFilter(null);
        table.load(filtered);
        countLabel.setText("Showing " + filtered.size() + " of " + allLogs.size() + " audit events");
    }

    private Object[] toRow(AuditLog l) {
        return new Object[]{
                l.timestamp() != null ? l.timestamp().format(FMT) : "-",
                l.actorId() != null ? l.actorId() : "-",
                l.actorRole() != null ? l.actorRole() : "-",
                l.action() != null ? l.action() : "-",
                l.targetType() != null ? l.targetType() : "-",
                l.targetId() != null ? l.targetId() : "-",
                l.details() != null ? l.details() : "-"
        };
    }
}
