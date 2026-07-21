package com.library.gui;

import com.library.config.ApplicationBootstrap;
import com.library.facade.LibraryFacade;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main application frame with sidebar navigation and card-layout content area.
 */
public final class MainFrame extends JFrame {

    private final LibraryFacade facade;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JPanel sidebarItems;
    private JPanel loginView;
    private JPanel mainView;
    private CardLayout rootLayout;
    private JPanel rootPanel;

    // Panels
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private BooksPanel booksPanel;
    private StudentsPanel studentsPanel;
    private BorrowPanel borrowPanel;
    private FinesPanel finesPanel;
    private SearchPanel searchPanel;

    private Session currentSession;
    private final Map<String, NavItem> navItems = new LinkedHashMap<>();
    private String activeNav = "Dashboard";

    public MainFrame() {
        facade = new LibraryFacade();
        ApplicationBootstrap.initialise(facade);

        setTitle("Library Management System — University Central Library");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setSize(1400, 850);
        setLocationRelativeTo(null);

        // Root layout: login vs main
        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);
        rootPanel.setBackground(AppTheme.BG_PRIMARY);

        // Login
        loginPanel = new LoginPanel(facade, this::onLoginSuccess);
        rootPanel.add(loginPanel, "LOGIN");

        // Main app view
        mainView = new JPanel(new BorderLayout());
        mainView.setBackground(AppTheme.BG_PRIMARY);

        // Sidebar
        JPanel sidebar = buildSidebar();
        mainView.add(sidebar, BorderLayout.WEST);

        // Content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppTheme.BG_PRIMARY);

        dashboardPanel = new DashboardPanel(facade);
        booksPanel = new BooksPanel(facade);
        studentsPanel = new StudentsPanel(facade);
        borrowPanel = new BorrowPanel(facade);
        finesPanel = new FinesPanel(facade);
        searchPanel = new SearchPanel(facade);

        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(booksPanel, "Books");
        contentPanel.add(studentsPanel, "Students");
        contentPanel.add(borrowPanel, "Borrows");
        contentPanel.add(finesPanel, "Fines");
        contentPanel.add(searchPanel, "Search");

        mainView.add(contentPanel, BorderLayout.CENTER);
        rootPanel.add(mainView, "MAIN");

        sidebarItems = (JPanel) sidebar.getComponent(1);

        setContentPane(rootPanel);
        rootLayout.show(rootPanel, "LOGIN");
    }

    private void onLoginSuccess() {
        currentSession = loginPanel.getSession();
        refreshAll();
        rootLayout.show(rootPanel, "MAIN");
        navigateTo("Dashboard");
    }

    private void refreshAll() {
        dashboardPanel.refresh(currentSession);
        booksPanel.refresh(currentSession);
        studentsPanel.refresh(currentSession);
        borrowPanel.refresh(currentSession);
        finesPanel.refresh(currentSession);
        searchPanel.refresh(currentSession);
    }

    private void navigateTo(String name) {
        activeNav = name;
        cardLayout.show(contentPanel, name);
        // Refresh the target panel
        switch (name) {
            case "Dashboard" -> dashboardPanel.refresh(currentSession);
            case "Books" -> booksPanel.refresh(currentSession);
            case "Students" -> studentsPanel.refresh(currentSession);
            case "Borrows" -> borrowPanel.refresh(currentSession);
            case "Fines" -> finesPanel.refresh(currentSession);
            case "Search" -> searchPanel.refresh(currentSession);
        }
        // Update sidebar highlight
        navItems.values().forEach(NavItem::updateState);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(AppTheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BorderLayout());

        // Logo header
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom border
                g2.setColor(AppTheme.BORDER);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        logoPanel.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 70));
        logoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 18));

        JLabel logoIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.ACCENT);
                g2.fillRoundRect(0, 0, 34, 34, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.drawString("LM", 5, 23);
            }
        };
        logoIcon.setPreferredSize(new Dimension(34, 34));

        JLabel logoText = new JLabel("Library Manager");
        logoText.setFont(AppTheme.FONT_BODY_BOLD);
        logoText.setForeground(AppTheme.TEXT_PRIMARY);

        logoPanel.add(logoIcon);
        logoPanel.add(logoText);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Nav items
        JPanel navPanel = new JPanel();
        navPanel.setBackground(AppTheme.SIDEBAR_BG);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        addNavItem(navPanel, "Dashboard", "\u2302");
        addNavItem(navPanel, "Books", "\uD83D\uDCD6");
        addNavItem(navPanel, "Students", "\uD83D\uDC65");
        addNavItem(navPanel, "Borrows", "\uD83D\uDD04");
        addNavItem(navPanel, "Fines", "\uD83D\uDCB0");
        addNavItem(navPanel, "Search", "\uD83D\uDD0D");

        sidebar.add(navPanel, BorderLayout.CENTER);

        // Bottom: Logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(AppTheme.SIDEBAR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 16, 8));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        // Separator
        JPanel sep = new JPanel();
        sep.setBackground(AppTheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        bottomPanel.add(sep);
        bottomPanel.add(Box.createVerticalStrut(8));

        NavItem logoutItem = new NavItem("Logout", "\u23FB", false);
        logoutItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logoutItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { doLogout(); }
        });
        bottomPanel.add(logoutItem);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel parent, String name, String icon) {
        NavItem item = new NavItem(name, icon, true);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { navigateTo(name); }
        });
        navItems.put(name, item);
        parent.add(item);
        parent.add(Box.createVerticalStrut(2));
    }

    private void doLogout() {
        if (currentSession != null) {
            try { facade.auth().logout(currentSession.token()); } catch (Exception ignored) {}
        }
        currentSession = null;
        loginPanel.reset();
        rootLayout.show(rootPanel, "LOGIN");
    }

    /**
     * A single navigation item in the sidebar.
     */
    class NavItem extends JPanel {
        private final String name;
        private final String icon;
        private final boolean tracked;
        private boolean hovered = false;

        NavItem(String name, String icon, boolean tracked) {
            this.name = name;
            this.icon = icon;
            this.tracked = tracked;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 16, 42));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void updateState() { repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            AppTheme.applyAntiAliasing(g);
            Graphics2D g2 = (Graphics2D) g;

            boolean isActive = tracked && name.equals(activeNav);

            if (isActive) {
                g2.setColor(AppTheme.SIDEBAR_ACTIVE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                // Left accent bar
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 4, 3, getHeight() - 8, 2, 2));
            } else if (hovered) {
                g2.setColor(AppTheme.SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            }

            // Icon
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            g2.setColor(isActive ? AppTheme.ACCENT : (hovered ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_SECONDARY));
            g2.drawString(icon, 14, 27);

            // Label
            g2.setFont(AppTheme.FONT_SIDEBAR);
            g2.setColor(isActive ? AppTheme.TEXT_PRIMARY : (hovered ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_SECONDARY));
            g2.drawString(name, 42, 27);
        }
    }

    /** Entry point for the GUI application. */
    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Override dialog colors
        UIManager.put("OptionPane.background", AppTheme.BG_SECONDARY);
        UIManager.put("Panel.background", AppTheme.BG_SECONDARY);
        UIManager.put("OptionPane.messageForeground", AppTheme.TEXT_PRIMARY);
        UIManager.put("TextField.background", AppTheme.BG_INPUT);
        UIManager.put("TextField.foreground", AppTheme.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", AppTheme.ACCENT);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
