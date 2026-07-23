package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.model.Librarian;
import com.library.model.User;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Librarian Management Panel — Manage library staff, permissions, and accounts.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class LibrarianManagementPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    private PaginatedTable<User> table;

    private static final String[] COLS = {
            "ID", "Full Name", "Username", "Email", "Phone", "Status", "Permissions"
    };

    public LibrarianManagementPanel(LibraryFacade facade, Session session) {
        this.facade  = facade;
        this.session = session;
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
        titleBlock.add(AppTheme.heading("Librarian Management"));
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(AppTheme.label2("Manage library staff accounts, permissions, and access control"));

        // ── Toolbar ───────────────────────────────────────────────────────────
        JButton addBtn      = AppTheme.primaryBtn("Add Librarian");
        JButton editBtn     = AppTheme.secondaryBtn("Edit");
        JButton permBtn     = AppTheme.secondaryBtn("Assign Permissions");
        JButton resetPwdBtn = AppTheme.secondaryBtn("Reset Password");
        JButton deactivBtn  = AppTheme.dangerBtn("Deactivate");
        JButton refreshBtn  = AppTheme.secondaryBtn("Refresh");

        addBtn.setPreferredSize(new Dimension(140, 38));
        editBtn.setPreferredSize(new Dimension(90, 38));
        permBtn.setPreferredSize(new Dimension(160, 38));
        resetPwdBtn.setPreferredSize(new Dimension(130, 38));
        deactivBtn.setPreferredSize(new Dimension(110, 38));
        refreshBtn.setPreferredSize(new Dimension(100, 38));

        addBtn.addActionListener(e      -> doAdd());
        editBtn.addActionListener(e     -> doEdit());
        permBtn.addActionListener(e     -> doAssignPermissions());
        resetPwdBtn.addActionListener(e -> doResetPassword());
        deactivBtn.addActionListener(e  -> doDeactivate());
        refreshBtn.addActionListener(e  -> refresh(session));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(permBtn);
        toolbar.add(resetPwdBtn);
        toolbar.add(deactivBtn);
        toolbar.add(refreshBtn);

        hdr.add(titleBlock, BorderLayout.WEST);
        hdr.add(toolbar, BorderLayout.EAST);

        // ── Paginated Table ───────────────────────────────────────────────────
        table = new PaginatedTable<>(COLS, this::toRow, 25);

        add(hdr, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    private Object[] toRow(User u) {
        String permissions = "";
        if (u instanceof Librarian lib) {
            permissions = lib.getPermissions().size() + " permission(s)";
        }
        return new Object[]{
                u.getId(),
                u.fullName(),
                u.getUsername(),
                u.getEmail() != null ? u.getEmail() : "-",
                u.getPhone() != null ? u.getPhone() : "-",
                u.isActive() ? "ACTIVE" : "INACTIVE",
                permissions
        };
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return facade.users().getAllUsers(session);
            }
            @Override
            protected void done() {
                try {
                    table.load(get());
                } catch (Exception ex) {
                    AppTheme.error(LibrarianManagementPanel.this,
                            "Failed to load librarians: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void doAdd() {
        try {
            facade.rbac().require(session, Permissions.LIBRARIAN_ADD);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        JTextField firstField = AppTheme.textField(15);
        JTextField lastField  = AppTheme.textField(15);
        JTextField emailField = AppTheme.textField(20);
        JTextField phoneField = AppTheme.textField(15);
        JTextField userField  = AppTheme.textField(15);
        JPasswordField pwdField = AppTheme.passwordField(15);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("First Name:")); form.add(firstField);
        form.add(new JLabel("Last Name:"));  form.add(lastField);
        form.add(new JLabel("Email:"));      form.add(emailField);
        form.add(new JLabel("Phone:"));      form.add(phoneField);
        form.add(new JLabel("Username:"));   form.add(userField);
        form.add(new JLabel("Password:"));   form.add(pwdField);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Add Librarian", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            facade.users().addLibrarian(session,
                    firstField.getText().trim(),
                    lastField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    userField.getText().trim(),
                    new String(pwdField.getPassword()),
                    new HashSet<>());
            AppTheme.success(this, "Librarian added successfully.");
            refresh(session);
        } catch (Exception ex) {
            AppTheme.error(this, "Failed to add librarian: " + ex.getMessage());
        }
    }

    private void doEdit() {
        try {
            facade.rbac().require(session, Permissions.LIBRARIAN_UPDATE);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        User selected = table.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Select a librarian from the table first.");
            return;
        }
        if (!(selected instanceof Librarian lib)) {
            AppTheme.error(this, "Selected user is not a librarian.");
            return;
        }

        JTextField emailField = AppTheme.textField(20);
        JTextField phoneField = AppTheme.textField(15);
        emailField.setText(lib.getEmail() != null ? lib.getEmail() : "");
        phoneField.setText(lib.getPhone() != null ? lib.getPhone() : "");

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Email:")); form.add(emailField);
        form.add(new JLabel("Phone:")); form.add(phoneField);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Edit Librarian: " + lib.fullName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            lib.setEmail(emailField.getText().trim());
            lib.setPhone(phoneField.getText().trim());
            facade.users().updateLibrarian(session, lib);
            AppTheme.success(this, "Librarian updated successfully.");
            refresh(session);
        } catch (Exception ex) {
            AppTheme.error(this, "Failed to update librarian: " + ex.getMessage());
        }
    }

    private void doAssignPermissions() {
        try {
            facade.rbac().require(session, Permissions.LIBRARIAN_ASSIGN_PERMISSIONS);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        User selected = table.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Select a librarian from the table first.");
            return;
        }

        Set<String> current = (selected instanceof Librarian lib)
                ? new HashSet<>(lib.getPermissions()) : new HashSet<>();

        // Build list of available permissions
        String[] allPerms = {
            Permissions.BOOK_ADD, Permissions.BOOK_UPDATE, Permissions.BOOK_DELETE,
            Permissions.BOOK_ARCHIVE, Permissions.BOOK_RESTORE, Permissions.BOOK_VIEW,
            Permissions.BORROW_ISSUE, Permissions.BORROW_RETURN, Permissions.BORROW_RENEW,
            Permissions.BORROW_VIEW_ALL,
            Permissions.STUDENT_ADD, Permissions.STUDENT_UPDATE, Permissions.STUDENT_VIEW,
            Permissions.FINE_VIEW_ALL, Permissions.FINE_WAIVE, Permissions.FINE_COLLECT,
            Permissions.RESERVATION_VIEW_ALL, Permissions.RESERVATION_CANCEL,
            Permissions.ANALYTICS_VIEW, Permissions.REPORT_VIEW, Permissions.REPORT_GENERATE
        };

        JList<String> permList = new JList<>(allPerms);
        permList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Pre-select current permissions
        int[] indices = java.util.stream.IntStream.range(0, allPerms.length)
                .filter(i -> current.contains(allPerms[i]))
                .toArray();
        permList.setSelectedIndices(indices);

        JScrollPane sp = new JScrollPane(permList);
        sp.setPreferredSize(new Dimension(320, 280));

        int result = JOptionPane.showConfirmDialog(this, sp,
                "Assign Permissions — " + selected.fullName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Set<String> chosen = new HashSet<>(permList.getSelectedValuesList());
            facade.users().assignPermissions(session, selected.getId(), chosen);
            AppTheme.success(this, "Permissions updated successfully.");
            refresh(session);
        } catch (Exception ex) {
            AppTheme.error(this, "Failed to assign permissions: " + ex.getMessage());
        }
    }

    private void doResetPassword() {
        try {
            facade.rbac().require(session, Permissions.LIBRARIAN_RESET_PASSWORD);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        User selected = table.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Select a librarian from the table first.");
            return;
        }

        JPasswordField pwdField = AppTheme.passwordField(15);
        JPanel form = new JPanel(new GridLayout(1, 2, 8, 8));
        form.add(new JLabel("New Password:")); form.add(pwdField);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Reset Password — " + selected.fullName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String newPwd = new String(pwdField.getPassword());
        if (newPwd.isBlank()) {
            AppTheme.error(this, "Password cannot be blank.");
            return;
        }

        try {
            facade.users().resetPassword(session, selected.getId(), newPwd);
            AppTheme.success(this, "Password reset successfully.");
        } catch (Exception ex) {
            AppTheme.error(this, "Failed to reset password: " + ex.getMessage());
        }
    }

    private void doDeactivate() {
        try {
            facade.rbac().require(session, Permissions.LIBRARIAN_UPDATE);
        } catch (Exception ex) {
            AppTheme.error(this, "Access denied: " + ex.getMessage());
            return;
        }

        User selected = table.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Select a librarian from the table first.");
            return;
        }

        if (!AppTheme.confirm(this,
                "Deactivate account for " + selected.fullName() + "?")) return;

        try {
            facade.users().deactivateUser(session, selected.getId());
            AppTheme.success(this, "Librarian deactivated.");
            refresh(session);
        } catch (Exception ex) {
            AppTheme.error(this, "Failed to deactivate: " + ex.getMessage());
        }
    }
}
