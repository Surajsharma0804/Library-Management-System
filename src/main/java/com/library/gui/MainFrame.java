package com.library.gui;

import com.library.config.ApplicationBootstrap;
import com.library.enums.UserRole;
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
 * Root application frame: sidebar navigation changes per role,
 * top tool-bar with theme toggle, and card-layout content area.
 *
 * @author University Central Library — Software Engineering Division
 * @version 1.0.0
 */
public final class MainFrame extends JFrame {

    private final LibraryFacade facade;
    private final CardLayout cards;
    private final JPanel content;
    private CardLayout rootCards;
    private JPanel root;

    private LoginPanel loginPanel;

    /* ── Panels (created lazily per role) ─────────────────────────── */
    private DashboardPanel   dashboardPanel;
    private BooksPanel       booksPanel;
    private StudentsPanel    studentsPanel;
    private BorrowPanel      borrowPanel;
    private FinesPanel       finesPanel;
    private SearchPanel      searchPanel;
    private StudentHomePanel studentHomePanel;
    private MyBorrowsPanel   myBorrowsPanel;
    private MyFinesPanel     myFinesPanel;

    private Session session;
    private final Map<String, NavItem> nav = new LinkedHashMap<>();
    private String active = "";
    private JPanel sidebar, topBar, main;

    public MainFrame() {
        facade = new LibraryFacade();
        ApplicationBootstrap.initialise(facade);

        setTitle("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setSize(1440, 860);
        setLocationRelativeTo(null);

        rootCards = new CardLayout();
        root = new JPanel(rootCards);

        loginPanel = new LoginPanel(facade, this::onLogin);
        root.add(loginPanel, "LOGIN");

        cards = new CardLayout();
        content = new JPanel(cards);
        content.setBackground(AppTheme.bg());

        setContentPane(root);
        rootCards.show(root, "LOGIN");

        AppTheme.onThemeChange(this::applyTheme);
    }

    /* ── Login callback — build UI per role ───────────────────────── */

    private void onLogin() {
        session = loginPanel.getSession();
        if (session == null) return;

        // Remove old main if exists
        if (main != null) root.remove(main);
        content.removeAll();
        nav.clear();

        main = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new BorderLayout());
        topBar = topBar();
        center.add(topBar, BorderLayout.NORTH);

        // Build role-specific panels and nav
        switch (session.role()) {
            case ADMIN -> buildAdminUI();
            case LIBRARIAN -> buildLibrarianUI();
            case STUDENT -> buildStudentUI();
        }

        center.add(content, BorderLayout.CENTER);
        sidebar = sidebar(session.role());
        main.add(sidebar, BorderLayout.WEST);
        main.add(center, BorderLayout.CENTER);
        root.add(main, "MAIN");

        rootCards.show(root, "MAIN");

        // Navigate to first item
        String first = nav.keySet().iterator().next();
        go(first);
    }

    private void buildAdminUI() {
        dashboardPanel  = new DashboardPanel(facade);
        booksPanel      = new BooksPanel(facade);
        studentsPanel   = new StudentsPanel(facade);
        borrowPanel     = new BorrowPanel(facade);
        finesPanel      = new FinesPanel(facade);
        searchPanel     = new SearchPanel(facade);

        content.add(dashboardPanel, "Dashboard");
        content.add(booksPanel,     "Book Catalogue");
        content.add(studentsPanel,  "Student Records");
        content.add(borrowPanel,    "Circulation");
        content.add(finesPanel,     "Fine Management");
        content.add(searchPanel,    "Search");
    }

    private void buildLibrarianUI() {
        dashboardPanel  = new DashboardPanel(facade);
        booksPanel      = new BooksPanel(facade);
        borrowPanel     = new BorrowPanel(facade);
        finesPanel      = new FinesPanel(facade);
        searchPanel     = new SearchPanel(facade);

        content.add(dashboardPanel, "Dashboard");
        content.add(booksPanel,     "Book Catalogue");
        content.add(borrowPanel,    "Circulation");
        content.add(finesPanel,     "Fine Collection");
        content.add(searchPanel,    "Search");
    }

    private void buildStudentUI() {
        studentHomePanel = new StudentHomePanel(facade);
        booksPanel       = new BooksPanel(facade);
        myBorrowsPanel   = new MyBorrowsPanel(facade);
        myFinesPanel     = new MyFinesPanel(facade);

        content.add(studentHomePanel, "My Dashboard");
        content.add(booksPanel,       "Browse Books");
        content.add(myBorrowsPanel,   "My Borrows");
        content.add(myFinesPanel,     "My Fines");
    }

    /* ── Navigation ──────────────────────────────────────────────── */

    private void go(String name) {
        active = name;
        cards.show(content, name);
        refreshPanel(name);
        nav.values().forEach(NavItem::refresh);
        setTopTitle(name);
    }

    private void refreshPanel(String name) {
        switch (name) {
            case "Dashboard"       -> { if (dashboardPanel  != null) dashboardPanel.refresh(session); }
            case "Book Catalogue", "Browse Books" -> { if (booksPanel != null) booksPanel.refresh(session); }
            case "Student Records" -> { if (studentsPanel   != null) studentsPanel.refresh(session); }
            case "Circulation"     -> { if (borrowPanel     != null) borrowPanel.refresh(session); }
            case "Fine Management", "Fine Collection" -> { if (finesPanel != null) finesPanel.refresh(session); }
            case "Search"          -> { if (searchPanel     != null) searchPanel.refresh(session); }
            case "My Dashboard"    -> { if (studentHomePanel!= null) studentHomePanel.refresh(session); }
            case "My Borrows"      -> { if (myBorrowsPanel  != null) myBorrowsPanel.refresh(session); }
            case "My Fines"        -> { if (myFinesPanel    != null) myFinesPanel.refresh(session); }
        }
    }

    /* ── Top bar ─────────────────────────────────────────────────── */

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g);
                g.setColor(AppTheme.bgCard());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(AppTheme.border());
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 20));
        bar.setOpaque(false);

        JLabel t = new JLabel("");
        t.setFont(AppTheme.H3); t.setForeground(AppTheme.fg());
        t.setName("_title");
        bar.add(t, BorderLayout.WEST);

        // Right: role badge + theme toggle
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);

        if (session != null) {
            JLabel roleBadge = new JLabel(session.role().name()) {
                @Override protected void paintComponent(Graphics g) {
                    AppTheme.aa(g); var g2 = (Graphics2D) g;
                    Color c = switch (session.role()) {
                        case ADMIN -> AppTheme.RED;
                        case LIBRARIAN -> AppTheme.ACCENT;
                        case STUDENT -> AppTheme.GREEN;
                    };
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(c);
                    g2.setFont(AppTheme.SMALL_B);
                    var fm = g2.getFontMetrics();
                    String txt = session.role().name();
                    g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
            };
            roleBadge.setPreferredSize(new Dimension(80, 28));
            right.add(roleBadge);

            JLabel userLabel = new JLabel(session.username());
            userLabel.setFont(AppTheme.BODY); userLabel.setForeground(AppTheme.fgSecondary());
            right.add(userLabel);
        }

        right.add(AppTheme.themeBtn());
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void setTopTitle(String s) {
        for (Component c : topBar.getComponents())
            if (c instanceof JLabel l && "_title".equals(l.getName())) {
                l.setText(s); l.setForeground(AppTheme.fg());
            }
    }

    /* ── Sidebar ─────────────────────────────────────────────────── */

    private JPanel sidebar(UserRole role) {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(AppTheme.sidebarBg());
        sb.setPreferredSize(new Dimension(AppTheme.SIDEBAR_W, 0));

        // logo
        JPanel logo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g);
                g.setColor(AppTheme.sidebarBg());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(255, 255, 255, 12));
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        logo.setPreferredSize(new Dimension(AppTheme.SIDEBAR_W, 56));
        logo.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 12));

        JLabel icon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                var gp = new GradientPaint(0, 0, AppTheme.ACCENT, 32, 32, AppTheme.VIOLET);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 32, 32, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString("LMS", 3, 21);
            }
        };
        icon.setPreferredSize(new Dimension(32, 32));

        JLabel brandText = new JLabel("Library Manager");
        brandText.setFont(AppTheme.BODY_B); brandText.setForeground(Color.WHITE);
        logo.add(icon); logo.add(brandText);
        sb.add(logo, BorderLayout.NORTH);

        // nav items — role specific
        JPanel items = new JPanel();
        items.setBackground(AppTheme.sidebarBg());
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBorder(BorderFactory.createEmptyBorder(12, 10, 10, 10));

        // Section label
        String sectionTitle = switch (role) {
            case ADMIN -> "ADMINISTRATION";
            case LIBRARIAN -> "LIBRARY OPERATIONS";
            case STUDENT -> "STUDENT PORTAL";
        };
        JLabel sec = new JLabel("  " + sectionTitle);
        sec.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sec.setForeground(new Color(255, 255, 255, 80));
        sec.setAlignmentX(Component.LEFT_ALIGNMENT);
        sec.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        items.add(sec); items.add(Box.createVerticalStrut(6));

        switch (role) {
            case ADMIN -> {
                addNav(items, "Dashboard",      "\u25A3");
                addNav(items, "Book Catalogue",  "\u25A1");
                addNav(items, "Student Records", "\u25CB");
                addNav(items, "Circulation",     "\u21C4");
                addNav(items, "Fine Management", "\u25C7");
                addNav(items, "Search",          "\u2315");
            }
            case LIBRARIAN -> {
                addNav(items, "Dashboard",      "\u25A3");
                addNav(items, "Book Catalogue",  "\u25A1");
                addNav(items, "Circulation",     "\u21C4");
                addNav(items, "Fine Collection", "\u25C7");
                addNav(items, "Search",          "\u2315");
            }
            case STUDENT -> {
                addNav(items, "My Dashboard",  "\u25A3");
                addNav(items, "Browse Books",  "\u25A1");
                addNav(items, "My Borrows",    "\u21C4");
                addNav(items, "My Fines",      "\u25C7");
            }
        }
        sb.add(items, BorderLayout.CENTER);

        // bottom — logout
        JPanel bottom = new JPanel();
        bottom.setBackground(AppTheme.sidebarBg());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 18, 10));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JPanel sep = new JPanel();
        sep.setBackground(new Color(255, 255, 255, 12));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottom.add(sep); bottom.add(Box.createVerticalStrut(10));

        NavItem logout = new NavItem("Sign Out", "\u2192", false);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { logout(); }
        });
        bottom.add(logout);
        sb.add(bottom, BorderLayout.SOUTH);
        return sb;
    }

    private void addNav(JPanel p, String name, String icon) {
        NavItem item = new NavItem(name, icon, true);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { go(name); }
        });
        nav.put(name, item);
        p.add(item); p.add(Box.createVerticalStrut(2));
    }

    private void logout() {
        if (session != null) try { facade.auth().logout(session.token()); } catch (Exception ignored) {}
        session = null;
        loginPanel.reset();
        rootCards.show(root, "LOGIN");
    }

    /* ── Theme ───────────────────────────────────────────────────── */

    private void applyTheme() {
        root.setBackground(AppTheme.bg());
        content.setBackground(AppTheme.bg());
        loginPanel.applyTheme();
        if (main != null && session != null) {
            main.remove(sidebar);
            sidebar = sidebar(session.role());
            main.add(sidebar, BorderLayout.WEST);
            var center = (Container) main.getComponent(1);
            center.remove(topBar);
            topBar = topBar();
            center.add(topBar, BorderLayout.NORTH);
            setTopTitle(active);
            refreshPanel(active);
        }
        SwingUtilities.invokeLater(() -> { revalidate(); repaint(); });
    }

    /* ── Sidebar nav item ────────────────────────────────────────── */

    class NavItem extends JPanel {
        private final String name, icon;
        private final boolean tracked;
        private boolean hover;

        NavItem(String name, String icon, boolean tracked) {
            this.name = name; this.icon = icon; this.tracked = tracked;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(AppTheme.SIDEBAR_W - 20, 42));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }
        void refresh() { repaint(); }
        @Override protected void paintComponent(Graphics g) {
            AppTheme.aa(g); var g2 = (Graphics2D) g;
            boolean act = tracked && name.equals(active);
            if (act) {
                g2.setColor(AppTheme.sidebarActive());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 6, 3, getHeight() - 12, 3, 3));
            } else if (hover) {
                g2.setColor(AppTheme.sidebarHover());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }
            Color col = act ? AppTheme.ACCENT : hover ? Color.WHITE : new Color(0x8B, 0x95, 0xA5);
            g2.setFont(AppTheme.SIDEBAR); g2.setColor(col);
            g2.drawString(icon, 16, 27);
            g2.setColor(act ? Color.WHITE : col);
            g2.drawString(name, 42, 27);
        }
    }

    /* ── Entry point ─────────────────────────────────────────────── */

    public static void launch() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
