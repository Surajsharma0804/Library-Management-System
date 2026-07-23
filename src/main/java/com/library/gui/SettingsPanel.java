package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Branch;
import com.library.model.LibraryConfig;
import com.library.model.MembershipTier;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.service.SqliteMigrationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Executive System Settings Panel — JTabbedPane with Library Settings,
 * Membership Tiers, Branches, and SQLite Migration tabs.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class SettingsPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    // Library Settings tab
    private JTextField loanField, renewField, borrowField, reserveField,
                       fineField, holdField, memberField, libNameField;

    // Membership Tiers tab
    private PaginatedTable<MembershipTier> tiersTable;

    // Branches tab
    private DefaultTableModel branchModel;
    private JTable branchTable;

    // SQLite Migration tab
    private JTextArea migrationOutput;

    public SettingsPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(AppTheme.heading("System Settings"));
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(AppTheme.label2("Library policies, membership tiers, branches, and data migration"));
        add(hdr, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.BODY_B);
        tabs.addTab("Library Settings",  buildLibrarySettingsTab());
        tabs.addTab("Membership Tiers",  buildMembershipTiersTab());
        tabs.addTab("Branches",          buildBranchesTab());
        tabs.addTab("SQLite Migration",  buildSqliteMigrationTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ── Tab 1: Library Settings ───────────────────────────────────────────────

    private JPanel buildLibrarySettingsTab() {
        JPanel outer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        outer.setOpaque(false);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.CARD_R, AppTheme.CARD_R));
                g2.setColor(AppTheme.border()); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-1, getHeight()-1, AppTheme.CARD_R, AppTheme.CARD_R));
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        loanField    = field(); renewField   = field();
        borrowField  = field(); reserveField = field();
        fineField    = field(); holdField    = field();
        memberField  = field(); libNameField = field();

        int r = 0;
        addRow(card, gbc, r++, "Library Name", libNameField, "Display name of this library");
        addRow(card, gbc, r++, "Standard Loan Period (days)", loanField, "Maximum loan duration");
        addRow(card, gbc, r++, "Max Renewal Limit", renewField, "Max renewals per loan");
        addRow(card, gbc, r++, "Student Borrow Quota", borrowField, "Max concurrent borrows");
        addRow(card, gbc, r++, "Student Reservation Quota", reserveField, "Max active reservations");
        addRow(card, gbc, r++, "Daily Overdue Fine (₹)", fineField, "Fine per day per overdue book");
        addRow(card, gbc, r++, "Reservation Hold Window (days)", holdField, "Days before ready reservation expires");
        addRow(card, gbc, r++, "Membership Validity (months)", memberField, "Default membership duration");

        gbc.gridx = 1; gbc.gridy = r; gbc.fill = GridBagConstraints.NONE;
        JButton saveBtn = AppTheme.primaryBtn("Save Policy Changes");
        saveBtn.setPreferredSize(new Dimension(170, 40));
        saveBtn.addActionListener(e -> saveLibrarySettings());
        card.add(saveBtn, gbc);

        outer.add(card);
        return outer;
    }

    private void addRow(JPanel card, GridBagConstraints gbc, int row,
                        String label, JTextField field, String desc) {
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        JPanel lblPanel = new JPanel();
        lblPanel.setOpaque(false);
        lblPanel.setLayout(new BoxLayout(lblPanel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label); l.setFont(AppTheme.BODY_B); l.setForeground(AppTheme.fg());
        JLabel d = new JLabel(desc);  d.setFont(AppTheme.SMALL);   d.setForeground(AppTheme.fgMuted());
        lblPanel.add(l); lblPanel.add(d);
        lblPanel.setPreferredSize(new Dimension(320, 38));
        card.add(lblPanel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        card.add(field, gbc);
        gbc.weightx = 0;
    }

    private JTextField field() {
        JTextField f = AppTheme.textField(10);
        f.setPreferredSize(new Dimension(160, 38));
        return f;
    }

    private void saveLibrarySettings() {
        try {
            facade.rbac().require(session, Permissions.CONFIG_UPDATE);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }
        try {
            facade.config().updateLoanPeriod(session, Integer.parseInt(loanField.getText().trim()));
            facade.config().updateMaxRenewals(session, Integer.parseInt(renewField.getText().trim()));
            facade.config().updateBorrowLimit(session, Integer.parseInt(borrowField.getText().trim()));
            facade.config().updateMaxReservations(session, Integer.parseInt(reserveField.getText().trim()));
            facade.config().updateFinePerDay(session, (long)(Double.parseDouble(fineField.getText().trim()) * 100));
            facade.config().updateReservationHoldDays(session, Integer.parseInt(holdField.getText().trim()));
            facade.config().updateMembershipMonths(session, Integer.parseInt(memberField.getText().trim()));
            String libName = libNameField.getText().trim();
            if (!libName.isEmpty()) {
                LibraryConfig cfg = facade.config().get();
                cfg.setLibraryName(libName);
                facade.config().updateConfig(cfg);
            }
            AppTheme.success(this, "Policy configuration updated successfully!");
        } catch (Exception ex) {
            AppTheme.error(this, "Invalid input: " + ex.getMessage());
        }
    }

    // ── Tab 2: Membership Tiers ───────────────────────────────────────────────

    private JPanel buildMembershipTiersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Toolbar
        JButton addBtn    = AppTheme.primaryBtn("Add Tier");
        JButton editBtn   = AppTheme.secondaryBtn("Edit");
        JButton deleteBtn = AppTheme.dangerBtn("Delete");

        addBtn.setPreferredSize(new Dimension(110, 38));
        editBtn.setPreferredSize(new Dimension(90, 38));
        deleteBtn.setPreferredSize(new Dimension(90, 38));

        addBtn.addActionListener(e    -> doAddTier());
        editBtn.addActionListener(e   -> doEditTier());
        deleteBtn.addActionListener(e -> doDeleteTier());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn);

        // Table
        tiersTable = new PaginatedTable<>(
                new String[]{"ID", "Tier Name", "Borrow Limit", "Loan Period (days)", "Renewals", "Max Reservations"},
                t -> new Object[]{
                        t.getId(), t.getTierName(), t.getBorrowLimit(),
                        t.getLoanPeriodDays(), t.getRenewalLimit(), t.getMaxActiveReservations()
                },
                25
        );

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(tiersTable, BorderLayout.CENTER);
        return panel;
    }

    private void doAddTier() {
        try { facade.rbac().require(session, Permissions.CONFIG_UPDATE); }
        catch (Exception ex) { AppTheme.error(this, "Access denied: " + ex.getMessage()); return; }

        JTextField name   = AppTheme.textField(12);
        JTextField borrow = AppTheme.textField(5);
        JTextField loan   = AppTheme.textField(5);
        JTextField renew  = AppTheme.textField(5);
        JTextField resv   = AppTheme.textField(5);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Tier Name:"));          form.add(name);
        form.add(new JLabel("Borrow Limit:"));       form.add(borrow);
        form.add(new JLabel("Loan Period (days):")); form.add(loan);
        form.add(new JLabel("Renewal Limit:"));      form.add(renew);
        form.add(new JLabel("Max Reservations:"));   form.add(resv);

        if (JOptionPane.showConfirmDialog(this, form, "Add Membership Tier",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            facade.membershipTiers().create(session, name.getText().trim(),
                    Integer.parseInt(borrow.getText().trim()),
                    Integer.parseInt(loan.getText().trim()),
                    Integer.parseInt(renew.getText().trim()),
                    Integer.parseInt(resv.getText().trim()));
            AppTheme.success(this, "Membership tier created.");
            refreshTiersTable();
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private void doEditTier() {
        try { facade.rbac().require(session, Permissions.CONFIG_UPDATE); }
        catch (Exception ex) { AppTheme.error(this, "Access denied: " + ex.getMessage()); return; }

        MembershipTier sel = tiersTable.getSelectedItem();
        if (sel == null) { AppTheme.error(this, "Select a tier first."); return; }

        JTextField name   = AppTheme.textField(12);
        JTextField borrow = AppTheme.textField(5);
        JTextField loan   = AppTheme.textField(5);
        JTextField renew  = AppTheme.textField(5);
        JTextField resv   = AppTheme.textField(5);

        name.setText(sel.getTierName());
        borrow.setText(String.valueOf(sel.getBorrowLimit()));
        loan.setText(String.valueOf(sel.getLoanPeriodDays()));
        renew.setText(String.valueOf(sel.getRenewalLimit()));
        resv.setText(String.valueOf(sel.getMaxActiveReservations()));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Tier Name:"));          form.add(name);
        form.add(new JLabel("Borrow Limit:"));       form.add(borrow);
        form.add(new JLabel("Loan Period (days):")); form.add(loan);
        form.add(new JLabel("Renewal Limit:"));      form.add(renew);
        form.add(new JLabel("Max Reservations:"));   form.add(resv);

        if (JOptionPane.showConfirmDialog(this, form, "Edit Membership Tier",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            facade.membershipTiers().update(session, sel.getId(), name.getText().trim(),
                    Integer.parseInt(borrow.getText().trim()),
                    Integer.parseInt(loan.getText().trim()),
                    Integer.parseInt(renew.getText().trim()),
                    Integer.parseInt(resv.getText().trim()));
            AppTheme.success(this, "Tier updated.");
            refreshTiersTable();
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private void doDeleteTier() {
        try { facade.rbac().require(session, Permissions.CONFIG_UPDATE); }
        catch (Exception ex) { AppTheme.error(this, "Access denied: " + ex.getMessage()); return; }

        MembershipTier sel = tiersTable.getSelectedItem();
        if (sel == null) { AppTheme.error(this, "Select a tier first."); return; }

        if (!AppTheme.confirm(this, "Delete tier '" + sel.getTierName() + "'?")) return;
        try {
            facade.membershipTiers().delete(session, sel.getId());
            AppTheme.success(this, "Tier deleted.");
            refreshTiersTable();
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private void refreshTiersTable() {
        try {
            List<MembershipTier> tiers = facade.membershipTiers().findAll(session);
            tiersTable.load(tiers);
        } catch (Exception ex) {
            AppTheme.error(this, "Could not load tiers: " + ex.getMessage());
        }
    }

    // ── Tab 3: Branches ───────────────────────────────────────────────────────

    private JPanel buildBranchesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton addBtn    = AppTheme.primaryBtn("Add Branch");
        JButton editBtn   = AppTheme.secondaryBtn("Edit");

        addBtn.setPreferredSize(new Dimension(120, 38));
        editBtn.setPreferredSize(new Dimension(90, 38));

        addBtn.addActionListener(e  -> doAddBranch());
        editBtn.addActionListener(e -> doEditBranch());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(addBtn); toolbar.add(editBtn);

        branchModel = new DefaultTableModel(
                new String[]{"ID", "Branch Name", "Location", "Phone"}, 0);
        branchTable = new JTable(branchModel) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        AppTheme.styleTable(branchTable);
        branchTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        branchTable.getColumnModel().getColumn(2).setPreferredWidth(220);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(AppTheme.scroll(branchTable), BorderLayout.CENTER);
        return panel;
    }

    private void doAddBranch() {
        try { facade.rbac().require(session, Permissions.CONFIG_UPDATE); }
        catch (Exception ex) { AppTheme.error(this, "Access denied: " + ex.getMessage()); return; }

        JTextField name     = AppTheme.textField(20);
        JTextField location = AppTheme.textField(20);
        JTextField phone    = AppTheme.textField(15);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Branch Name:")); form.add(name);
        form.add(new JLabel("Location:"));    form.add(location);
        form.add(new JLabel("Phone:"));       form.add(phone);

        if (JOptionPane.showConfirmDialog(this, form, "Add Branch",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            facade.branches().create(session,
                    name.getText().trim(), location.getText().trim(), phone.getText().trim());
            AppTheme.success(this, "Branch created.");
            refreshBranchTable();
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private void doEditBranch() {
        try { facade.rbac().require(session, Permissions.CONFIG_UPDATE); }
        catch (Exception ex) { AppTheme.error(this, "Access denied: " + ex.getMessage()); return; }

        int row = branchTable.getSelectedRow();
        if (row < 0) { AppTheme.error(this, "Select a branch first."); return; }

        String id       = (String) branchModel.getValueAt(row, 0);
        String curName  = (String) branchModel.getValueAt(row, 1);
        String curLoc   = (String) branchModel.getValueAt(row, 2);
        String curPhone = (String) branchModel.getValueAt(row, 3);

        JTextField nameField  = AppTheme.textField(20);
        JTextField locField   = AppTheme.textField(20);
        JTextField phoneField = AppTheme.textField(15);
        nameField.setText(curName  != null ? curName  : "");
        locField.setText(curLoc    != null ? curLoc   : "");
        phoneField.setText(curPhone != null ? curPhone : "");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Branch Name:")); form.add(nameField);
        form.add(new JLabel("Location:"));    form.add(locField);
        form.add(new JLabel("Phone:"));       form.add(phoneField);

        if (JOptionPane.showConfirmDialog(this, form, "Edit Branch",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            facade.branches().update(session, id,
                    nameField.getText().trim(), locField.getText().trim(), phoneField.getText().trim());
            AppTheme.success(this, "Branch updated.");
            refreshBranchTable();
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private void refreshBranchTable() {
        try {
            List<Branch> branches = facade.branches().findAll(session);
            branchModel.setRowCount(0);
            for (Branch b : branches) {
                branchModel.addRow(new Object[]{
                        b.getId(), b.getBranchName(),
                        b.getLocation() != null ? b.getLocation() : "-",
                        b.getPhone() != null ? b.getPhone() : "-"
                });
            }
        } catch (Exception ex) {
            // silently ignore if permission denied
        }
    }

    // ── Tab 4: SQLite Migration ───────────────────────────────────────────────

    private JPanel buildSqliteMigrationTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel title = AppTheme.heading("SQLite Database Migration");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel desc1 = AppTheme.label2("Migrate all JSON data stores to a SQLite database backend.");
        desc1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel desc2 = AppTheme.label2("This operation reads all JSON files and writes them to SQLite tables.");
        desc2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton migrateBtn = AppTheme.primaryBtn("Run Migration");
        migrateBtn.setPreferredSize(new Dimension(160, 42));
        migrateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        migrateBtn.addActionListener(e -> doMigrate());

        info.add(title);
        info.add(Box.createVerticalStrut(8));
        info.add(desc1);
        info.add(desc2);
        info.add(Box.createVerticalStrut(16));
        info.add(migrateBtn);

        migrationOutput = new JTextArea(12, 60);
        migrationOutput.setEditable(false);
        migrationOutput.setFont(new Font("Consolas", Font.PLAIN, 12));
        migrationOutput.setBackground(AppTheme.bgCard());
        migrationOutput.setForeground(AppTheme.fg());
        migrationOutput.setText("Migration output will appear here after running...");

        panel.add(info, BorderLayout.NORTH);
        panel.add(AppTheme.scroll(migrationOutput), BorderLayout.CENTER);
        return panel;
    }

    private void doMigrate() {
        if (!AppTheme.confirm(this,
                "Migrate all data from JSON to SQLite?\nThis may take a few moments.")) return;

        migrationOutput.setText("Starting migration...\n");
        JButton btn = new JButton(); // disabled state placeholder

        new SwingWorker<SqliteMigrationService.MigrationResult, Void>() {
            @Override
            protected SqliteMigrationService.MigrationResult doInBackground() {
                return facade.sqliteMigration().migrate(session);
            }
            @Override
            protected void done() {
                try {
                    var result = get();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Migration completed.\n\n");
                    sb.append("Stores migrated successfully:\n");
                    result.rowCounts().forEach((store, count) ->
                            sb.append("  ✓ ").append(store).append(": ").append(count).append(" records\n"));
                    if (!result.errors().isEmpty()) {
                        sb.append("\nErrors encountered:\n");
                        result.errors().forEach(e -> sb.append("  ✗ ").append(e).append("\n"));
                    } else {
                        sb.append("\nAll stores migrated without errors.");
                    }
                    migrationOutput.setText(sb.toString());

                    String msg = result.errors().isEmpty()
                            ? "Migration completed successfully! " + result.rowCounts().size() + " stores migrated."
                            : "Migration completed with " + result.errors().size() + " error(s). See output for details.";
                    if (result.errors().isEmpty()) {
                        AppTheme.success(SettingsPanel.this, msg);
                    } else {
                        AppTheme.error(SettingsPanel.this, msg);
                    }
                } catch (Exception ex) {
                    migrationOutput.setText("Migration failed: " + ex.getMessage());
                    AppTheme.error(SettingsPanel.this, "Migration failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());

        // Check CONFIG_VIEW permission
        try {
            facade.rbac().require(session, Permissions.CONFIG_VIEW);
        } catch (Exception ex) {
            removeAll();
            setLayout(new BorderLayout());
            JLabel denied = new JLabel("Access restricted — CONFIG_VIEW permission required.");
            denied.setFont(AppTheme.H3); denied.setForeground(AppTheme.RED);
            denied.setHorizontalAlignment(SwingConstants.CENTER);
            add(denied, BorderLayout.CENTER);
            revalidate(); repaint();
            return;
        }

        // Load Library Settings
        try {
            LibraryConfig c = facade.config().get();
            libNameField.setText(c.getLibraryName() != null ? c.getLibraryName() : "");
            loanField.setText(String.valueOf(c.getLoanPeriodDays()));
            renewField.setText(String.valueOf(c.getMaxRenewals()));
            borrowField.setText(String.valueOf(c.getDefaultBorrowLimit()));
            reserveField.setText(String.valueOf(c.getMaxReservations()));
            fineField.setText(String.format("%.2f", c.getFinePerDayPaise() / 100.0));
            holdField.setText(String.valueOf(c.getReservationHoldDays()));
            memberField.setText(String.valueOf(c.getMembershipMonths()));
        } catch (Exception ignored) {}

        // Load Membership Tiers
        refreshTiersTable();

        // Load Branches
        refreshBranchTable();
    }
}
