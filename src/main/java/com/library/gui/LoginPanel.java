package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Authentication screen with role selector, branded identity, and
 * smooth gradient background. Supports light / dark theme toggle.
 *
 * @author University Central Library — Software Engineering Division
 * @version 1.0.0
 */
public final class LoginPanel extends JPanel {

    private final LibraryFacade facade;
    private final Runnable onSuccess;
    private JTextField userField;
    private JPasswordField passField;
    private JComboBox<String> roleBox;
    private JLabel errLabel;
    private String token;

    public LoginPanel(LibraryFacade facade, Runnable onSuccess) {
        this.facade = facade;
        this.onSuccess = onSuccess;
        setLayout(new GridBagLayout());
        build();
    }

    /* ── UI construction ─────────────────────────────────────────── */

    private void build() {
        removeAll();
        setBackground(AppTheme.bg());

        // card container
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g);
                var g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                // soft shadow
                g2.setColor(new Color(0, 0, 0, AppTheme.isDark() ? 50 : 18));
                g2.fill(new RoundRectangle2D.Float(6, 6, w - 6, h - 6, 24, 24));
                // card body
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 6, h - 6, 24, 24));
                // light-mode border
                if (!AppTheme.isDark()) {
                    g2.setColor(AppTheme.border());
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(.5f, .5f, w - 7, h - 7, 24, 24));
                }
                // top accent gradient
                var gp = new GradientPaint(0, 0, AppTheme.ACCENT, w, 0, AppTheme.VIOLET);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 6, 4, 4, 4));
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(440, 620));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 44, 36, 44));

        // theme toggle — top right
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        topRow.add(AppTheme.themeBtn());

        // logo circle
        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g);
                var g2 = (Graphics2D) g;
                var gp = new GradientPaint(0, 0, AppTheme.ACCENT, 64, 64, AppTheme.VIOLET);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 64, 64);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                var fm = g2.getFontMetrics();
                String s = "LMS";
                g2.drawString(s, (64 - fm.stringWidth(s)) / 2, (64 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        logo.setPreferredSize(new Dimension(64, 64));
        logo.setMaximumSize(new Dimension(64, 64));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = lbl("University Central Library", AppTheme.LOGO, AppTheme.fg());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = lbl("Library Management System", AppTheme.SMALL, AppTheme.fgSecondary());
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // role selector
        JLabel roleLbl = fieldLabel("Sign in as");
        roleBox = AppTheme.comboBox("Administrator", "Librarian", "Student");
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // username
        JLabel userLbl = fieldLabel("Username");
        userField = AppTheme.textField(20);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // password
        JLabel passLbl = fieldLabel("Password");
        passField = AppTheme.passwordField(20);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // error
        errLabel = new JLabel(" ");
        errLabel.setFont(AppTheme.SMALL);
        errLabel.setForeground(AppTheme.RED);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // sign-in button
        JButton signIn = AppTheme.primaryBtn("Sign In");
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signIn.addActionListener(this::login);
        passField.addActionListener(this::login);
        userField.addActionListener(e -> passField.requestFocusInWindow());

        // version
        JLabel ver = lbl("Version 1.0.0", AppTheme.SMALL, AppTheme.fgMuted());
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(topRow);
        card.add(logo);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(roleLbl); card.add(Box.createVerticalStrut(6)); card.add(roleBox);
        card.add(Box.createVerticalStrut(14));
        card.add(userLbl); card.add(Box.createVerticalStrut(6)); card.add(userField);
        card.add(Box.createVerticalStrut(14));
        card.add(passLbl); card.add(Box.createVerticalStrut(6)); card.add(passField);
        card.add(Box.createVerticalStrut(4));
        card.add(errLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(signIn);
        card.add(Box.createVerticalStrut(14));
        card.add(ver);

        add(card);
        revalidate(); repaint();
    }

    /* ── Helpers ──────────────────────────────────────────────────── */

    private JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AppTheme.SMALL_B); l.setForeground(AppTheme.fgSecondary());
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel lbl(String t, Font f, Color c) {
        var l = new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }

    private void login(ActionEvent e) {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) { errLabel.setText("Please enter both username and password."); return; }
        try {
            token = facade.auth().login(user, pass);
            errLabel.setText(" ");
            onSuccess.run();
        } catch (Exception ex) {
            errLabel.setText("Invalid credentials. Please try again.");
            passField.setText(""); passField.requestFocusInWindow();
        }
    }

    /* ── Public API ──────────────────────────────────────────────── */

    public String  getToken()    { return token; }
    public Session getSession()  { return token == null ? null : facade.sessions().require(token); }
    public String  getRole()     { return (String) roleBox.getSelectedItem(); }

    public void reset() {
        userField.setText(""); passField.setText(""); errLabel.setText(" ");
        userField.requestFocusInWindow(); token = null;
    }

    public void applyTheme() {
        String saved = userField != null ? userField.getText() : "";
        build();
        userField.setText(saved);
    }
}
