package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Backup & Restore Panel — Create and manage full-system backups.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class BackupPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    private JTable table;
    private DefaultTableModel model;

    private static final String[] COLS = {"Backup ID / Path", "Timestamp", "Size (KB)"};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BackupPanel(LibraryFacade facade, Session session) {
        this.facade  = facade;
        this.session = session;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        // ── Access Guard ──────────────────────────────────────────────────────
        boolean canAccess;
        try {
            facade.rbac().require(session, Permissions.BACKUP_CREATE);
            canAccess = true;
        } catch (Exception ex) {
            canAccess = false;
        }

        if (!canAccess) {
            setLayout(new BorderLayout());
            JLabel restricted = new JLabel("Access restricted — BACKUP_CREATE permission required.");
            restricted.setFont(AppTheme.H3);
            restricted.setForeground(AppTheme.RED);
            restricted.setHorizontalAlignment(SwingConstants.CENTER);
            add(restricted, BorderLayout.CENTER);
            return;
        }

        // ── Header ────────────────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(AppTheme.heading("Backup & Restore"));
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(AppTheme.label2("Create full-system backups and restore from any snapshot"));

        JButton createBtn  = AppTheme.primaryBtn("Create Backup");
        JButton restoreBtn = AppTheme.dangerBtn("Restore Selected");
        JButton refreshBtn = AppTheme.secondaryBtn("Refresh");

        createBtn.setPreferredSize(new Dimension(140, 38));
        restoreBtn.setPreferredSize(new Dimension(140, 38));
        refreshBtn.setPreferredSize(new Dimension(100, 38));

        createBtn.addActionListener(e  -> doCreate());
        restoreBtn.addActionListener(e -> doRestore());
        refreshBtn.addActionListener(e -> refresh(session));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(createBtn);
        toolbar.add(restoreBtn);
        toolbar.add(refreshBtn);

        hdr.add(titleBlock, BorderLayout.WEST);
        hdr.add(toolbar, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        AppTheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(340);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        add(hdr, BorderLayout.NORTH);
        add(AppTheme.scroll(table), BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        if (model == null) return;
        model.setRowCount(0);
        List<Path> backups = facade.backup().listBackups();
        for (Path path : backups) {
            long sizeKb = computeSizeKb(path);
            LocalDateTime ts = parseTimestamp(path.getFileName().toString());
            String tsStr = ts != null ? ts.format(FMT) : path.getFileName().toString();
            model.addRow(new Object[]{path.toAbsolutePath().toString(), tsStr, sizeKb});
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void doCreate() {
        try {
            facade.rbac().require(session, Permissions.BACKUP_CREATE);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        JButton btn = AppTheme.primaryBtn("Create Backup");
        btn.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                return facade.backup().createBackup(session);
            }
            @Override protected void done() {
                try {
                    String path = get();
                    AppTheme.success(BackupPanel.this, "Backup created successfully.\nLocation: " + path);
                    refresh(session);
                } catch (Exception ex) {
                    AppTheme.error(BackupPanel.this, "Backup failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void doRestore() {
        try {
            facade.rbac().require(session, Permissions.BACKUP_RESTORE);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            AppTheme.error(this, "Select a backup entry from the table first.");
            return;
        }

        String backupPath = (String) model.getValueAt(row, 0);
        if (!AppTheme.confirm(this,
                "WARNING: Restoring will overwrite current data.\n\nRestore from:\n" + backupPath + "\n\nProceed?")) {
            return;
        }

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                return facade.backup().restoreBackup(session, backupPath);
            }
            @Override protected void done() {
                try {
                    get();
                    AppTheme.success(BackupPanel.this, "Restore completed successfully.\nSource: " + backupPath);
                    refresh(session);
                } catch (Exception ex) {
                    AppTheme.error(BackupPanel.this, "Restore failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long computeSizeKb(Path dir) {
        try (var walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile)
                    .mapToLong(p -> { try { return Files.size(p); } catch (Exception e) { return 0L; } })
                    .sum() / 1024L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private LocalDateTime parseTimestamp(String dirName) {
        // Expected format: backup-yyyyMMdd-HHmmss
        try {
            if (dirName.startsWith("backup-")) {
                String ts = dirName.substring("backup-".length());
                return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
