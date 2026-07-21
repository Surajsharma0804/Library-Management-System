package com.library.gui;

import com.library.facade.LibraryFacade;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Professional login screen with role selector and branding.
 */
public final class LoginPanel extends JPanel {

    private final LibraryFacade facade;
    private final Runnable onLoginSuccess;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JLabel errorLabel;
    private String currentToken;

    public LoginPanel(LibraryFacade facade, Runnable onLoginSuccess) {
        this.facade = facade;
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        removeAll();
        setBackground(AppTheme.bgPrimary());

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                // Shadow
                g2.setColor(new Color(0, 0, 0, AppTheme.isDarkMode() ? 40 : 15));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 20, 20));
                // Card
                g2.setColor(AppTheme.bgSecondary());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                // Border in light mode
                if (!AppTheme.isDarkMode()) {
                    g2.setColor(AppTheme.border());
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 20, 20));
                }
                // Top accent
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, 4, 4, 4));
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(430, 600));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        // Logo
        JLabel iconLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
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
        title.setFont(AppTheme.FONT_LOGO); title.setForeground(AppTheme.textPrimary());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Library Management System v1.0.0");
        subtitle.setFont(AppTheme.FONT_SMALL); subtitle.setForeground(AppTheme.textSecondary());
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role selector
        JLabel roleLabel = makeFieldLabel("Login As");
        roleCombo = AppTheme.styledComboBox(new String[]{"Administrator", "Librarian", "Student"});
        roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username
        JLabel userLabel = makeFieldLabel("Username");
        usernameField = AppTheme.styledTextField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = makeFieldLabel("Password");
        passwordField = AppTheme.styledPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error
        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton loginBtn = AppTheme.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(this::doLogin);
        passwordField.addActionListener(this::doLogin);
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        // Theme toggle at top-right of card
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        topBar.add(AppTheme.themeToggleButton());

        card.add(topBar);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(roleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(roleCombo);
        card.add(Box.createVerticalStrut(16));
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

        add(card);
        revalidate(); repaint();
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.textSecondary());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void doLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            currentToken = facade.auth().login(username, password);
            errorLabel.setText(" ");
            onLoginSuccess.run();
        } catch (Exception ex) {
            errorLabel.setText("Invalid username or password.");
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    public String getToken() { return currentToken; }

    public Session getSession() {
        if (currentToken == null) return null;
        return facade.sessions().require(currentToken);
    }

    public String getSelectedRole() {
        return (String) roleCombo.getSelectedItem();
    }

    public void reset() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        usernameField.requestFocusInWindow();
        currentToken = null;
    }

    /** Rebuilds UI for theme change. */
    public void applyTheme() {
        String savedUser = usernameField != null ? usernameField.getText() : "";
        buildUI();
        usernameField.setText(savedUser);
    }
}
