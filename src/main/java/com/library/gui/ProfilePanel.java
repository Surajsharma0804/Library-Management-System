package com.library.gui;

import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.Student;
import com.library.model.User;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Profile Panel — Displays read-only account details and provides a
 * "Change Password" dialog. Accessible to all roles.
 */
public final class ProfilePanel extends JPanel {

    private final LibraryFacade facade;

    // Profile display fields (read-only)
    private JTextField fullNameField, emailField, phoneField;
    private JTextField departmentField, programField, regNoField;
    private JTextField membershipStatusField, membershipExpiryField, borrowCountField;

    // Inline status message
    private JLabel messageLabel;

    // Session token captured on refresh
    private String currentToken;

    public ProfilePanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        removeAll();

        // ── Header ──────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(AppTheme.heading("My Profile"));
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(AppTheme.label2("View and manage your account details"));
        hdr.add(titlePanel, BorderLayout.WEST);

        JButton changePwdBtn = AppTheme.primaryBtn("Change Password");
        changePwdBtn.setPreferredSize(new Dimension(160, 40));
        changePwdBtn.addActionListener(e -> openChangePasswordDialog());
        hdr.add(changePwdBtn, BorderLayout.EAST);

        add(hdr, BorderLayout.NORTH);

        // ── Profile card ─────────────────────────────────────────────
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        fullNameField         = readOnlyField();
        emailField            = readOnlyField();
        phoneField            = readOnlyField();
        departmentField       = readOnlyField();
        programField          = readOnlyField();
        regNoField            = readOnlyField();
        membershipStatusField = readOnlyField();
        membershipExpiryField = readOnlyField();
        borrowCountField      = readOnlyField();

        String[][] rows = {
            {"Full Name",            null},
            {"Email",                null},
            {"Phone",                null},
            {"Department",           null},
            {"Program / Course",     null},
            {"Registration No.",     null},
            {"Membership Status",    null},
            {"Membership Expiry",    null},
            {"Current Borrow Count", null},
        };
        JTextField[] fields = {
            fullNameField, emailField, phoneField,
            departmentField, programField, regNoField,
            membershipStatusField, membershipExpiryField, borrowCountField
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridy = i;
            gbc.gridx = 0; gbc.weightx = 0.3;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(AppTheme.BODY_B);
            lbl.setForeground(AppTheme.fgSecondary());
            card.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.7;
            card.add(fields[i], gbc);
        }

        // Inline message
        messageLabel = new JLabel(" ");
        messageLabel.setFont(AppTheme.SMALL_B);
        messageLabel.setForeground(AppTheme.GREEN);
        gbc.gridy = rows.length;
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        card.add(messageLabel, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JTextField readOnlyField() {
        JTextField f = AppTheme.textField(20);
        f.setEditable(false);
        f.setFocusable(false);
        f.setBackground(AppTheme.bgCard());
        f.setForeground(AppTheme.fg());
        return f;
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        AppTheme.CARD_R, AppTheme.CARD_R));
                g2.setColor(AppTheme.border());
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1,
                        AppTheme.CARD_R, AppTheme.CARD_R));
            }
        };
        card.setOpaque(false);
        return card;
    }

    public void refresh(Session session) {
        this.currentToken = session.token();
        setBackground(AppTheme.bg());
        messageLabel.setText(" ");

        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() {
                String[] vals = new String[9];
                java.util.Arrays.fill(vals, "-");
                try {
                    if (session.role() == UserRole.STUDENT) {
                        Student s = facade.userRepo().findStudentByUsername(session.username());
                        if (s != null) {
                            vals[0] = s.getFirstName() + (s.getLastName() != null ? " " + s.getLastName() : "");
                            vals[1] = s.getEmail() != null ? s.getEmail() : "-";
                            vals[2] = s.getPhone() != null ? s.getPhone() : "-";
                            vals[3] = s.getDepartment() != null ? s.getDepartment() : "-";
                            vals[4] = s.getProgram() != null ? s.getProgram()
                                      : (s.getCourse() != null ? s.getCourse() : "-");
                            vals[5] = s.getRegistrationNumber();
                            vals[6] = s.getMembershipStatus().name();
                            vals[7] = s.getMembershipExpiry() != null
                                      ? s.getMembershipExpiry().toString() : "-";
                            vals[8] = String.valueOf(s.getCurrentBorrowCount());
                        }
                    } else {
                        User u = facade.staffRepo().findByUsername(session.username());
                        if (u != null) {
                            vals[0] = u.getFirstName() + (u.getLastName() != null ? " " + u.getLastName() : "");
                            vals[1] = u.getEmail() != null ? u.getEmail() : "-";
                            vals[2] = u.getPhone() != null ? u.getPhone() : "-";
                            vals[3] = "-";
                            vals[4] = session.role().name();
                            vals[5] = u.getId();
                            vals[6] = "STAFF";
                            vals[7] = "-";
                            vals[8] = "-";
                        }
                    }
                } catch (Exception ignored) {}
                return vals;
            }

            @Override
            protected void done() {
                try {
                    String[] vals = get();
                    fullNameField.setText(vals[0]);
                    emailField.setText(vals[1]);
                    phoneField.setText(vals[2]);
                    departmentField.setText(vals[3]);
                    programField.setText(vals[4]);
                    regNoField.setText(vals[5]);
                    membershipStatusField.setText(vals[6]);
                    membershipExpiryField.setText(vals[7]);
                    borrowCountField.setText(vals[8]);
                } catch (Exception ignored) {}
                revalidate(); repaint();
            }
        }.execute();
    }

    private void openChangePasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        JPasswordField currentPwd = new JPasswordField(20);
        JPasswordField newPwd     = new JPasswordField(20);
        JPasswordField confirmPwd = new JPasswordField(20);

        JLabel currentLbl = new JLabel("Current Password:");
        JLabel newLbl     = new JLabel("New Password:");
        JLabel confirmLbl = new JLabel("Confirm Password:");
        for (JLabel l : new JLabel[]{currentLbl, newLbl, confirmLbl}) {
            l.setFont(AppTheme.BODY_B);
        }

        panel.add(currentLbl);  panel.add(currentPwd);
        panel.add(newLbl);      panel.add(newPwd);
        panel.add(confirmLbl);  panel.add(confirmPwd);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String oldPwd = new String(currentPwd.getPassword());
        String np     = new String(newPwd.getPassword());
        String cp     = new String(confirmPwd.getPassword());

        // Validate
        if (!np.equals(cp)) {
            showError("New password and confirmation do not match.");
            return;
        }
        if (np.length() < 8) {
            showError("Password must be at least 8 characters long.");
            return;
        }
        if (!np.chars().anyMatch(Character::isDigit)) {
            showError("Password must contain at least one digit.");
            return;
        }
        if (!np.chars().anyMatch(Character::isLetter)) {
            showError("Password must contain at least one letter.");
            return;
        }

        if (currentToken == null) {
            showError("Session expired. Please log in again.");
            return;
        }

        try {
            facade.auth().changePassword(currentToken, oldPwd, np);
            messageLabel.setForeground(AppTheme.GREEN);
            messageLabel.setText("Password changed successfully.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        messageLabel.setForeground(AppTheme.RED);
        messageLabel.setText(msg);
        AppTheme.error(this, msg);
    }
}
