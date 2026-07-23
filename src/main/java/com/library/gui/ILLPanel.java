package com.library.gui;

import com.library.enums.ILLDirection;
import com.library.enums.ILLStatus;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.InterLibraryLoan;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * ILL Panel — Inter-Library Loan management.
 * Requirements: 25.1
 */
public final class ILLPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    private PaginatedTable<InterLibraryLoan> table;

    public ILLPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        removeAll();

        // Header
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("Inter-Library Loans"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Manage ILL requests and fulfillment"));
        hdr.add(title, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton createBtn = AppTheme.primaryBtn("Create ILL");
        createBtn.setPreferredSize(new Dimension(110, 38));
        createBtn.addActionListener(e -> doCreateILL());
        toolbar.add(createBtn);

        JButton updateBtn = AppTheme.secondaryBtn("Update Status");
        updateBtn.setPreferredSize(new Dimension(130, 38));
        updateBtn.addActionListener(e -> doUpdateStatus());
        toolbar.add(updateBtn);

        hdr.add(toolbar, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // Table
        table = new PaginatedTable<>(
                new String[]{"Direction", "Partner Library", "Book Title", "ISBN", "Status",
                             "Requested Date", "Expected Return"},
                ill -> new Object[]{
                        ill.getDirection().name(),
                        ill.getPartnerLibraryName(),
                        ill.getBookTitle(),
                        ill.getBookIsbn() != null ? ill.getBookIsbn() : "-",
                        ill.getStatus().name(),
                        ill.getRequestedDate().toString(),
                        ill.getExpectedReturnDate() != null ? ill.getExpectedReturnDate().toString() : "-"
                },
                25
        );

        add(table, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        this.session = session;
        setBackground(AppTheme.bg());

        // STUDENT not allowed
        if (session.role() == UserRole.STUDENT) {
            removeAll();
            setLayout(new BorderLayout());
            JLabel denied = new JLabel("Access restricted — ILL management is not available for students.");
            denied.setFont(AppTheme.H3);
            denied.setForeground(AppTheme.RED);
            denied.setHorizontalAlignment(SwingConstants.CENTER);
            add(denied, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // Check ILL_MANAGE permission
        try {
            facade.rbac().require(session, Permissions.ILL_MANAGE);
        } catch (Exception ex) {
            removeAll();
            setLayout(new BorderLayout());
            JLabel denied = new JLabel("Access restricted — ILL_MANAGE permission required.");
            denied.setFont(AppTheme.H3);
            denied.setForeground(AppTheme.RED);
            denied.setHorizontalAlignment(SwingConstants.CENTER);
            add(denied, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // Load data in SwingWorker
        new SwingWorker<List<InterLibraryLoan>, Void>() {
            @Override
            protected List<InterLibraryLoan> doInBackground() {
                return facade.ill().findAll(session);
            }

            @Override
            protected void done() {
                try {
                    table.load(get());
                } catch (Exception e) {
                    AppTheme.error(ILLPanel.this, "Failed to load ILL records: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void doCreateILL() {
        if (session == null) return;

        JComboBox<String> directionBox = AppTheme.comboBox("INBOUND", "OUTBOUND");
        directionBox.setPreferredSize(new Dimension(200, 34));

        JTextField partnerField = AppTheme.textField(20);
        JTextField titleField = AppTheme.textField(20);
        JTextField isbnField = AppTheme.textField(15);
        JTextField requestedByField = AppTheme.textField(15);
        JTextField expectedReturnField = AppTheme.textField(12);
        expectedReturnField.setToolTipText("yyyy-MM-dd");
        JTextField notesField = AppTheme.textField(30);

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.add(new JLabel("Direction*:"));
        form.add(directionBox);
        form.add(new JLabel("Partner Library*:"));
        form.add(partnerField);
        form.add(new JLabel("Book Title*:"));
        form.add(titleField);
        form.add(new JLabel("Book ISBN:"));
        form.add(isbnField);
        form.add(new JLabel("Requested By (reg no):"));
        form.add(requestedByField);
        form.add(new JLabel("Expected Return (yyyy-MM-dd):"));
        form.add(expectedReturnField);
        form.add(new JLabel("Notes:"));
        form.add(notesField);

        int result = JOptionPane.showConfirmDialog(this, form, "Create Inter-Library Loan",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String partner = partnerField.getText().trim();
        String title = titleField.getText().trim();
        if (partner.isEmpty()) {
            AppTheme.error(this, "Partner Library name is required.");
            return;
        }
        if (title.isEmpty()) {
            AppTheme.error(this, "Book Title is required.");
            return;
        }

        ILLDirection direction = ILLDirection.valueOf((String) directionBox.getSelectedItem());
        String isbn = isbnField.getText().trim().isEmpty() ? null : isbnField.getText().trim();
        String requestedBy = requestedByField.getText().trim().isEmpty() ? null : requestedByField.getText().trim();
        String notes = notesField.getText().trim().isEmpty() ? null : notesField.getText().trim();

        LocalDate expectedReturn = null;
        String retDateStr = expectedReturnField.getText().trim();
        if (!retDateStr.isEmpty()) {
            try {
                expectedReturn = LocalDate.parse(retDateStr);
            } catch (DateTimeParseException e) {
                AppTheme.error(this, "Expected Return Date must be in yyyy-MM-dd format.");
                return;
            }
        }

        final LocalDate finalExpectedReturn = expectedReturn;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                facade.ill().create(session, direction, partner, title, isbn,
                        requestedBy, finalExpectedReturn, notes);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    AppTheme.success(ILLPanel.this, "ILL record created successfully.");
                    refresh(session);
                } catch (Exception e) {
                    AppTheme.error(ILLPanel.this, "Failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void doUpdateStatus() {
        if (session == null) return;

        InterLibraryLoan selected = table.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Please select an ILL record first.");
            return;
        }

        JComboBox<String> statusBox = AppTheme.comboBox("ACTIVE", "RETURNED", "CANCELLED");
        statusBox.setPreferredSize(new Dimension(200, 34));
        JTextField notesField = AppTheme.textField(30);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("New Status:"));
        form.add(statusBox);
        form.add(new JLabel("Notes (optional):"));
        form.add(notesField);

        int result = JOptionPane.showConfirmDialog(this, form, "Update ILL Status",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        ILLStatus newStatus = ILLStatus.valueOf((String) statusBox.getSelectedItem());
        String notes = notesField.getText().trim().isEmpty() ? null : notesField.getText().trim();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                facade.ill().updateStatus(session, selected.getId(), newStatus, notes);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    AppTheme.success(ILLPanel.this, "ILL status updated.");
                    refresh(session);
                } catch (Exception e) {
                    AppTheme.error(ILLPanel.this, "Failed: " + e.getMessage());
                }
            }
        }.execute();
    }
}
