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
 * Main application frame with sidebar, top bar with theme toggle, and card-layout content.
 */
public final class MainFrame extends JFrame {

    private final LibraryFacade facade;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private CardLayout rootLayout;
    private JPanel rootPanel;

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
    private JPanel sidebar;
    private JPanel topBar;
    private JPanel mainView;

    public MainFrame() {
        facade = new LibraryFacade();
        ApplicationBootstrap.initialise(facade);

        setTitle("Library Management System \u2014 University Central Library");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setSize(1400, 850);
        setLocationRelativeTo(null);

        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);

        // Login
        loginPanel = new LoginPanel(facade, this::onLoginSuccess);
        rootPanel.add(loginPanel, "LOGIN");

        // Main view
        mainView = new JPanel(new BorderLayout());

        // Sidebar
        sidebar = buildSidebar();
        mainView.add(sidebar, BorderLayout.WEST);

        // Center = topBar + content
        JPanel centerArea = new JPanel(new BorderLayout());
        topBar = buildTopBar();
        centerArea.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

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

        centerArea.add(contentPanel, BorderLayout.CENTER);
        mainView.add(centerArea, BorderLayout.CENTER);

        rootPanel.add(mainView, "MAIN");
        setContentPane(rootPanel);
        rootLayout.show(rootPanel, "LOGIN");

        // Theme listener - rebuild everything on toggle
        AppTheme.addThemeListener(this::applyTheme);
        applyThemeColors();
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
        switch (name) {
            case "Dashboard" -> dashboardPanel.refresh(currentSession);
            case "Books" -> booksPanel.refresh(currentSession);
            case "Students" -> studentsPanel.refresh(currentSession);
            case "Borrows" -> borrowPanel.refresh(currentSession);
            case "Fines" -> finesPanel.refresh(currentSession);
            case "Search" -> searchPanel.refresh(currentSession);
        }
        navItems.values().forEach(NavItem::updateState);
        updateTopBarTitle(name);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                g.setColor(AppTheme.bgSecondary());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(AppTheme.border());
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 16));
        bar.setOpaque(false);

        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(AppTheme.FONT_SUBHEADING);
        pageTitle.setForeground(AppTheme.textPrimary());
        pageTitle.setName("pageTitle");
        bar.add(pageTitle, BorderLayout.WEST);

        // Right side: theme toggle + user info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        rightPanel.setOpaque(false);

        JButton themeBtn = AppTheme.themeToggleButton();
        rightPanel.add(themeBtn);

        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    private void updateTopBarTitle(String name) {
        for (Component c : topBar.getComponents()) {
            if (c instanceof JLabel lbl && "pageTitle".equals(lbl.getName())) {
                lbl.setText(name);
                lbl.setForeground(AppTheme.textPrimary());
            }
        }
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(AppTheme.sidebarBg());
        sb.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        sb.setLayout(new BorderLayout());

        // Logo
        JPanel logoPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                g.setColor(AppTheme.sidebarBg());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(AppTheme.border());
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        logoPanel.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 52));
        logoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 12));

        JLabel logoIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.ACCENT);
                g2.fillRoundRect(0, 0, 30, 30, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("LM", 4, 21);
            }
        };
        logoIcon.setPreferredSize(new Dimension(30, 30));

        JLabel logoText = new JLabel("Library Manager");
        logoText.setFont(AppTheme.FONT_BODY_BOLD);
        logoText.setForeground(Color.WHITE);

        logoPanel.add(logoIcon);
        logoPanel.add(logoText);
        sb.add(logoPanel, BorderLayout.NORTH);

        // Nav items
        JPanel navPanel = new JPanel();
        navPanel.setBackground(AppTheme.sidebarBg());
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        navItems.clear();
        addNavItem(navPanel, "Dashboard", "\u2302");
        addNavItem(navPanel, "Books", "\uD83D\uDCD6");
        addNavItem(navPanel, "Students", "\uD83D\uDC65");
        addNavItem(navPanel, "Borrows", "\uD83D\uDD04");
        addNavItem(navPanel, "Fines", "\uD83D\uDCB0");
        addNavItem(navPanel, "Search", "\uD83D\uDD0D");

        sb.add(navPanel, BorderLayout.CENTER);

        // Bottom: Logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(AppTheme.sidebarBg());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 16, 8));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel sep = new JPanel();
        sep.setBackground(AppTheme.border());
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottomPanel.add(sep);
        bottomPanel.add(Box.createVerticalStrut(8));

        NavItem logoutItem = new NavItem("Logout", "\u23FB", false);
        logoutItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logoutItem.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { doLogout(); }
        });
        bottomPanel.add(logoutItem);
        sb.add(bottomPanel, BorderLayout.SOUTH);

        return sb;
    }

    private void addNavItem(JPanel parent, String name, String icon) {
        NavItem item = new NavItem(name, icon, true);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { navigateTo(name); }
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

    private void applyTheme() {
        applyThemeColors();
        loginPanel.applyTheme();
        // Rebuild sidebar
        mainView.remove(sidebar);
        sidebar = buildSidebar();
        mainView.add(sidebar, BorderLayout.WEST);
        // Rebuild top bar
        Container centerArea = (Container) mainView.getComponent(1);
        centerArea.remove(topBar);
        topBar = buildTopBar();
        centerArea.add(topBar, BorderLayout.NORTH);
        updateTopBarTitle(activeNav);
        // Refresh panels
        if (currentSession != null) refreshAll();
        SwingUtilities.invokeLater(() -> { revalidate(); repaint(); });
    }

    private void applyThemeColors() {
        rootPanel.setBackground(AppTheme.bgPrimary());
        contentPanel.setBackground(AppTheme.bgPrimary());
    }

    class NavItem extends JPanel {
        private final String name;
        private final String icon;
        private final boolean tracked;
        private boolean hovered = false;

        NavItem(String name, String icon, boolean tracked) {
            this.name = name; this.icon = icon; this.tracked = tracked;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 16, 42));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }
        void updateState() { repaint(); }
        @Override protected void paintComponent(Graphics g) {
            AppTheme.applyAntiAliasing(g);
            Graphics2D g2 = (Graphics2D) g;
            boolean isActive = tracked && name.equals(activeNav);
            if (isActive) {
                g2.setColor(AppTheme.sidebarActive());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 4, 3, getHeight() - 8, 2, 2));
            } else if (hovered) {
                g2.setColor(AppTheme.sidebarHover());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            }
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            g2.setColor(isActive ? AppTheme.ACCENT : (hovered ? Color.WHITE : new Color(0x8B, 0x94, 0x9E)));
            g2.drawString(icon, 14, 27);
            g2.setFont(AppTheme.FONT_SIDEBAR);
            g2.setColor(isActive ? Color.WHITE : (hovered ? Color.WHITE : new Color(0x8B, 0x94, 0x9E)));
            g2.drawString(name, 42, 27);
        }
    }

    public static void launch() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("OptionPane.background", AppTheme.bgSecondary());
        UIManager.put("Panel.background", AppTheme.bgSecondary());
        UIManager.put("OptionPane.messageForeground", AppTheme.textPrimary());
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
