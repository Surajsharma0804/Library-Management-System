package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Professional login screen with branding and animated form.
 */
public final class LoginPanel extends JPanel {

    private final LibraryFacade facade;
    private final Runnable onLoginSuccess;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;
    private String currentToken;

    public LoginPanel(LibraryFacade facade, Runnable onLoginSuccess) {
        this.facade = facade;
        this.onLoginSuccess = onLoginSuccess;
        setBackground(AppTheme.BG_PRIMARY);
        setLayout(new GridBagLayout());

        // Center card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 20, 20));
                g2.setColor(AppTheme.BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, 4, 4, 4));
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 520));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Logo icon
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.ACCENT_DARK);
                g2.fillOval(0, 0, 64, 64);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                String s = "LM";
                g2.drawString(s, (64 - fm.stringWidth(s)) / 2, (64 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        iconLabel.setPreferredSize(new Dimension(64, 64));
        iconLabel.setMaximumSize(new Dimension(64, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("University Central Library");
        title.setFont(AppTheme.FONT_LOGO);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Library Management System");
        subtitle.setFont(AppTheme.FONT_SMALL);
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(AppTheme.FONT_SMALL);
        userLabel.setForeground(AppTheme.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField = AppTheme.styledTextField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(AppTheme.FONT_SMALL);
        passLabel.setForeground(AppTheme.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = AppTheme.styledPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton loginBtn = AppTheme.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(this::doLogin);

        passwordField.addActionListener(this::doLogin);
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        // Version label
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(AppTheme.FONT_SMALL);
        versionLabel.setForeground(AppTheme.TEXT_MUTED);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(32));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(6));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(versionLabel);

        add(card);
    }

    private void doLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            // login() returns a token string
            currentToken = facade.auth().login(username, password);
            errorLabel.setText(" ");
            onLoginSuccess.run();
        } catch (Exception ex) {
            errorLabel.setText("Invalid username or password.");
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    /** Returns the token from the last successful login. */
    public String getToken() {
        return currentToken;
    }

    /** Gets the full Session object using the stored token. */
    public Session getSession() {
        if (currentToken == null) return null;
        return facade.sessions().require(currentToken);
    }

    public void reset() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        usernameField.requestFocusInWindow();
        currentToken = null;
    }
}
