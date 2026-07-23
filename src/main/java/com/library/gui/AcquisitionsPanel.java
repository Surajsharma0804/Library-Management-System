package com.library.gui;

import com.library.enums.AcquisitionStatus;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.Acquisition;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Acquisitions Panel — Book acquisition requests and approval workflow.
 * Requirements: 22.1, 22.2
 */
public final class AcquisitionsPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    private PaginatedTable<Acquisition> table;
    private JComboBox<String> statusFilter;

    public AcquisitionsPanel(LibraryFacade facade) {
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
        title.add(AppTheme.heading("Acquisitions"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Manage book acquisition requests and approvals"));

        hdr.add(title, BorderLayout.WEST);

        // Toolbar (right side)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton newRequestBtn = AppTheme.primaryBtn("New Request");
        newRequestBtn.setPreferredSize(new Dimension(130, 38));
        newRequestBtn.addActionListener(e -> doNewRequest());
        toolbar.add(newRequestBtn);

        JButton approveBtn = AppTheme.secondaryBtn("Approve");
        approveBtn.setPreferredSize(new Dimension(100, 38));
        approveBtn.addActionListener(e -> doApprove());
        toolbar.add(approveBtn);

        JButton rejectBtn = AppTheme.dangerBtn("Reject");
        rejectBtn.setPreferredSize(new Dimension(90, 38));
        rejectBtn.addActionListener(e -> doReject());
        toolbar.add(rejectBtn);

        hdr.add(toolbar, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterBar.setOpaque(false);
        filterBar.add(AppTheme.label("Status:"));
        statusFilter = AppTheme.comboBox("All", "PENDING", "APPROVED", "REJECTED", "RECEIVED");
        statusFilter.setPreferredSize(new Dimension(150, 34));
        statusFilter.addActionListener(e -> applyFilter());
        filterBar.add(statusFilter);

        // Table
        table = new PaginatedTable<>(
                new String[]{"Title", "Author", "ISBN", "Qty", "Cost (₹)", "Status", "Requested Date"},
                acq -> new Object[]{
                        acq.getRequestedTitle(),
                        acq.getAuthor() != null ? acq.getAuthor() : "-",
                        acq.getIsbn() != null ? acq.getIsbn() : "-",
                        acq.getQuantity(),
                        formatCost(acq.getEstimatedCostPaise()),
                        acq.getStatus().name(),
                        acq.getRequestedDate().toString()
                },
                25
        );

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(filterBar, BorderLayout.NORTH);
        center.add(table, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        this.session = session;
        setBackground(AppTheme.bg());

        // Check permission
        try {
            facade.rbac().require(session, Permissions.ACQUISITION_REQUEST);
        } catch (Exception ex) {
            removeAll();
            setLayout(new BorderLayout());
            JLabel denied = new JLabel("Access restricted — ACQUISITION_REQUEST permission required.");
            denied.setFont(AppTheme.H3);
            denied.setForeground(AppTheme.RED);
            denied.setHorizontalAlignment(SwingConstants.CENTER);
            add(denied, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // Load data in SwingWorker
        new SwingWorker<List<Acquisition>, Void>() {
            @Override
            protected List<Acquisition> doInBackground() {
                return facade.acquisitions().findAllForSession(session);
            }

            @Override
            protected void done() {
                try {
                    List<Acquisition> data = get();
                    table.load(data);
                    applyFilter(); // Apply any active filter
                } catch (Exception e) {
                    AppTheme.error(AcquisitionsPanel.this, "Failed to load acquisitions: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String selected = (String) statusFilter.getSelectedItem();
        if ("All".equals(selected)) {
            table.setFilter(null);
        } else {
            AcquisitionStatus status = AcquisitionStatus.valueOf(selected);
            table.setFilter(acq -> acq.getStatus() == status);
        }
    }

    private void doNewRequest() {
        if (session == null) return;

        JTextField titleField = AppTheme.textField(20);
        JTextField authorField = AppTheme.textField(20);
        JTextField isbnField = AppTheme.textField(15);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JSpinner costSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 100));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Title*:"));
        form.add(titleField);
        form.add(new JLabel("Author:"));
        form.add(authorField);
        form.add(new JLabel("ISBN:"));
        form.add(isbnField);
        form.add(new JLabel("Quantity*:"));
        form.add(qtySpinner);
        form.add(new JLabel("Estimated Cost (paise):"));
        form.add(costSpinner);

        int result = JOptionPane.showConfirmDialog(this, form, "New Acquisition Request",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            AppTheme.error(this, "Title is required.");
            return;
        }

        try {
            facade.acquisitions().submitRequest(
                    session,
                    title,
                    authorField.getText().trim().isEmpty() ? null : authorField.getText().trim(),
                    isbnField.getText().trim().isEmpty() ? null : isbnField.getText().trim(),
                    (int) qtySpinner.getValue(),
                    ((Number) costSpinner.getValue()).longValue()
            );
            AppTheme.success(this, "Acquisition request submitted.");
            refresh(session);
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private void doApprove() {
        if (session == null) return;
        
        // Check admin permission
        if (session.role() != UserRole.ADMIN) {
            AppTheme.error(this, "Only administrators can approve acquisition requests.");
            return;
        }

        Acquisition selected = table.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Please select an acquisition request first.");
            return;
        }

        if (selected.getStatus() != AcquisitionStatus.PENDING) {
            AppTheme.error(this, "Only PENDING requests can be approved.");
            return;
        }

        String notes = JOptionPane.showInputDialog(this, "Reviewer notes (optional):");
        if (notes == null) return; // cancelled

        try {
            facade.acquisitions().approve(session, selected.getId(), notes.isEmpty() ? null : notes);
            AppTheme.success(this, "Acquisition request approved.");
            refresh(session);
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private void doReject() {
        if (session == null) return;
        
        // Check admin permission
        if (session.role() != UserRole.ADMIN) {
            AppTheme.error(this, "Only administrators can reject acquisition requests.");
            return;
        }

        Acquisition selected = table.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Please select an acquisition request first.");
            return;
        }

        if (selected.getStatus() != AcquisitionStatus.PENDING) {
            AppTheme.error(this, "Only PENDING requests can be rejected.");
            return;
        }

        String notes = JOptionPane.showInputDialog(this, "Rejection reason:");
        if (notes == null) return; // cancelled

        try {
            facade.acquisitions().reject(session, selected.getId(), notes.isEmpty() ? null : notes);
            AppTheme.success(this, "Acquisition request rejected.");
            refresh(session);
        } catch (Exception ex) {
            AppTheme.error(this, "Failed: " + ex.getMessage());
        }
    }

    private String formatCost(long paise) {
        return String.format("₹%.2f", paise / 100.0);
    }
}
