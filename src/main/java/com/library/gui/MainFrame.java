package com.library.gui;

import com.library.config.ApplicationBootstrap;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.security.Session;
import com.library.service.OverdueJob;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executive Root Frame — Sidebar navigation, top header toolbar with user identity,
 * dynamic theme toggle, and card-layout content view.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class MainFrame extends JFrame {

    private final LibraryFacade facade;
    private final CardLayout cards;
    private final JPanel content;
    private CardLayout rootCards;
    private JPanel root;

    private LoginPanel loginPanel;

    /* ── Panels (created lazily per role) ─────────────────────────── */
    private DashboardPanel            dashboardPanel;
    private BooksPanel                booksPanel;
    private StudentsPanel             studentsPanel;
    private LibrarianManagementPanel  librarianPanel;
    private BorrowPanel               borrowPanel;
    private FinesPanel                finesPanel;
    private ReservationsPanel         reservationsPanel;
    private AnalyticsPanel            analyticsPanel;
    private ReportsPanel              reportsPanel;
    private AuditLogPanel             auditLogPanel;
    private SettingsPanel             settingsPanel;
    private BackupPanel               backupPanel;
    private SearchPanel               searchPanel;
    private StudentHomePanel          studentHomePanel;
    private MyBorrowsPanel            myBorrowsPanel;
    private MyFinesPanel              myFinesPanel;
    private MyReservationsPanel       myReservationsPanel;
    private NotificationsPanel        notificationsPanel;
    private ProfilePanel              profilePanel;
    private AcquisitionsPanel         acquisitionsPanel;
    private ILLPanel                  illPanel;
    private RoomReservationPanel      roomReservationPanel;

    private Session session;
    private final Map<String, NavItem> nav = new LinkedHashMap<>();
    private String active = "";
    private JPanel sidebar, topBar, main;

    public MainFrame() {
        AppTheme.initLookAndFeel();
        facade = new LibraryFacade();
        ApplicationBootstrap.initialise(facade);

        // Start background overdue-reminder job
        OverdueJob overdueJob = new OverdueJob(
                facade.borrowRepo(), facade.bookRepo(), facade.notificationPublisher());
        overdueJob.start();
        Runtime.getRuntime().addShutdownHook(
                new Thread(overdueJob::stop, "overdue-job-shutdown"));

        setTitle("Library Management System — Enterprise Edition");
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
        dashboardPanel   = new DashboardPanel(facade);
        booksPanel       = new BooksPanel(facade);
        studentsPanel    = new StudentsPanel(facade);
        librarianPanel   = new LibrarianManagementPanel(facade, session);
        borrowPanel      = new BorrowPanel(facade);
        finesPanel       = new FinesPanel(facade);
        reservationsPanel = new ReservationsPanel(facade);
        analyticsPanel   = new AnalyticsPanel(facade);
        reportsPanel     = new ReportsPanel(facade);
        auditLogPanel    = new AuditLogPanel(facade);
        settingsPanel    = new SettingsPanel(facade);
        backupPanel      = new BackupPanel(facade, session);
        searchPanel      = new SearchPanel(facade);

        content.add(dashboardPanel,   "Dashboard");
        content.add(booksPanel,       "Books");
        content.add(studentsPanel,    "Students");
        content.add(librarianPanel,   "Librarian Management");
        content.add(borrowPanel,      "Circulation");
        content.add(finesPanel,       "Fines");
        content.add(reservationsPanel,"Reservations");
        content.add(analyticsPanel,   "Analytics");
        content.add(reportsPanel,     "Reports");
        content.add(auditLogPanel,    "Audit Logs");
        content.add(settingsPanel,    "Settings");
        content.add(backupPanel,      "Backup");
        content.add(searchPanel,      "Search");
    }

    private void buildLibrarianUI() {
        dashboardPanel    = new DashboardPanel(facade);
        booksPanel        = new BooksPanel(facade);
        studentsPanel     = new StudentsPanel(facade);
        borrowPanel       = new BorrowPanel(facade);
        reservationsPanel = new ReservationsPanel(facade);
        finesPanel        = new FinesPanel(facade);
        reportsPanel      = new ReportsPanel(facade);
        analyticsPanel    = new AnalyticsPanel(facade);
        searchPanel       = new SearchPanel(facade);
        acquisitionsPanel = new AcquisitionsPanel(facade);
        illPanel          = new ILLPanel(facade);

        content.add(dashboardPanel,    "Dashboard");
        content.add(booksPanel,        "Book Catalogue");
        content.add(studentsPanel,     "Students");
        content.add(borrowPanel,       "Circulation");
        content.add(reservationsPanel, "Reservations");
        content.add(finesPanel,        "Fine Collection");
        content.add(reportsPanel,      "Reports");
        content.add(analyticsPanel,    "Analytics");
        content.add(searchPanel,       "Search");
        content.add(acquisitionsPanel, "Acquisitions");
        content.add(illPanel,          "ILL");
    }

    private void buildStudentUI() {
        studentHomePanel   = new StudentHomePanel(facade);
        booksPanel         = new BooksPanel(facade);
        myBorrowsPanel     = new MyBorrowsPanel(facade);
        myReservationsPanel = new MyReservationsPanel(facade);
        myFinesPanel       = new MyFinesPanel(facade);
        notificationsPanel = new NotificationsPanel(facade);
        profilePanel       = new ProfilePanel(facade);
        roomReservationPanel = new RoomReservationPanel(facade);

        content.add(studentHomePanel,    "My Dashboard");
        content.add(booksPanel,          "Browse Books");
        content.add(myBorrowsPanel,      "My Borrows");
        content.add(myReservationsPanel, "My Reservations");
        content.add(myFinesPanel,        "My Fines");
        content.add(notificationsPanel,  "Notifications");
        content.add(profilePanel,        "My Profile");
        content.add(roomReservationPanel,"Room Reservations");
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
            case "Dashboard"              -> { if (dashboardPanel      != null) dashboardPanel.refresh(session); }
            case "Books"                  -> { if (booksPanel          != null) booksPanel.refresh(session); }
            case "Browse Books"           -> { if (booksPanel          != null) booksPanel.refresh(session); }
            case "Students"               -> { if (studentsPanel       != null) studentsPanel.refresh(session); }
            case "Librarian Management"   -> { if (librarianPanel      != null) librarianPanel.refresh(session); }
            case "Circulation"            -> { if (borrowPanel         != null) borrowPanel.refresh(session); }
            case "Fines"                  -> { if (finesPanel          != null) finesPanel.refresh(session); }
            case "Reservations"           -> { if (reservationsPanel   != null) reservationsPanel.refresh(session); }
            case "Analytics"              -> { if (analyticsPanel      != null) analyticsPanel.refresh(session); }
            case "Reports"                -> { if (reportsPanel        != null) reportsPanel.refresh(session); }
            case "Audit Logs"             -> { if (auditLogPanel       != null) auditLogPanel.refresh(session); }
            case "Settings"               -> { if (settingsPanel       != null) settingsPanel.refresh(session); }
            case "Backup"                 -> { if (backupPanel         != null) backupPanel.refresh(session); }
            case "Search"                 -> { if (searchPanel         != null) searchPanel.refresh(session); }
            case "My Dashboard"           -> { if (studentHomePanel    != null) studentHomePanel.refresh(session); }
            case "My Borrows"             -> { if (myBorrowsPanel      != null) myBorrowsPanel.refresh(session); }
            case "My Fines"               -> { if (myFinesPanel        != null) myFinesPanel.refresh(session); }
            case "My Reservations"        -> { if (myReservationsPanel != null) myReservationsPanel.refresh(session); }
            case "Notifications"          -> { if (notificationsPanel  != null) notificationsPanel.refresh(session); }
            case "My Profile"             -> { if (profilePanel        != null) profilePanel.refresh(session); }
            case "Room Reservations"      -> { if (roomReservationPanel!= null) roomReservationPanel.refresh(session); }
            // Librarian nav aliases
            case "Book Catalogue"         -> { if (booksPanel          != null) booksPanel.refresh(session); }
            case "Fine Collection", "Fine Management" -> { if (finesPanel != null) finesPanel.refresh(session); }
            case "Acquisitions"           -> { if (acquisitionsPanel   != null) acquisitionsPanel.refresh(session); }
            case "ILL"                    -> { if (illPanel            != null) illPanel.refresh(session); }
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
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 20));
        bar.setOpaque(false);

        JLabel t = new JLabel("");
        t.setFont(AppTheme.H3); t.setForeground(AppTheme.fg());
        t.setName("_title");
        bar.add(t, BorderLayout.WEST);

        // Right: role badge + user info + theme toggle
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
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
                    g2.setColor(AppTheme.isDark() ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 40) : new Color(c.getRed(), c.getGreen(), c.getBlue(), 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(c);
                    g2.setFont(AppTheme.SMALL_B);
                    var fm = g2.getFontMetrics();
                    String txt = session.role().name();
                    g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
            };
            roleBadge.setPreferredSize(new Dimension(84, 26));
            right.add(roleBadge);

            JLabel userLabel = new JLabel(session.username());
            userLabel.setFont(AppTheme.BODY_B); userLabel.setForeground(AppTheme.fg());
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

        // logo header
        JPanel logo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g);
                g.setColor(AppTheme.sidebarBg());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(255, 255, 255, 15));
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        logo.setPreferredSize(new Dimension(AppTheme.SIDEBAR_W, 52));
        logo.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 10));

        JLabel icon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                AppTheme.aa(g); var g2 = (Graphics2D) g;
                g2.setColor(AppTheme.ACCENT);
                g2.fillRoundRect(0, 0, 30, 30, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString("LMS", 3, 20);
            }
        };
        icon.setPreferredSize(new Dimension(30, 30));

        JLabel brandText = new JLabel("Library Manager");
        brandText.setFont(AppTheme.BODY_B); brandText.setForeground(Color.WHITE);
        logo.add(icon); logo.add(brandText);
        sb.add(logo, BorderLayout.NORTH);

        // nav items — role specific
        JPanel items = new JPanel();
        items.setBackground(AppTheme.sidebarBg());
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBorder(BorderFactory.createEmptyBorder(14, 10, 10, 10));

        // Section label
        String sectionTitle = switch (role) {
            case ADMIN -> "ADMINISTRATION";
            case LIBRARIAN -> "LIBRARY OPERATIONS";
            case STUDENT -> "STUDENT PORTAL";
        };
        JLabel sec = new JLabel("  " + sectionTitle);
        sec.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sec.setForeground(new Color(148, 163, 184, 160));
        sec.setAlignmentX(Component.LEFT_ALIGNMENT);
        sec.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        items.add(sec); items.add(Box.createVerticalStrut(6));

        switch (role) {
            case ADMIN -> {
                addNav(items, "Dashboard",             "\u25A3");
                addNav(items, "Books",                 "\u25A1");
                addNav(items, "Students",              "\u25CB");
                addNav(items, "Librarian Management",  "\u25D0");
                addNav(items, "Circulation",           "\u21C4");
                addNav(items, "Fines",                 "\u25C7");
                addNav(items, "Reservations",          "\u27BF");
                addNav(items, "Analytics",             "\u25A3");
                addNav(items, "Reports",               "\u25AC");
                addNav(items, "Audit Logs",            "\u2394");
                addNav(items, "Settings",              "\u2699");
                addNav(items, "Backup",                "\u2601");
                addNav(items, "Search",                "\u2315");
            }
            case LIBRARIAN -> {
                addNav(items, "Dashboard",      "\u25A3");
                addNav(items, "Book Catalogue", "\u25A1");
                addNav(items, "Students",       "\u25CB");
                addNav(items, "Circulation",    "\u21C4");
                addNav(items, "Reservations",   "\u27BF");
                addNav(items, "Fine Collection","\u25C7");
                addNav(items, "Reports",        "\u25AC");
                addNav(items, "Analytics",      "\u25A3");
                addNav(items, "Search",         "\u2315");
            }
            case STUDENT -> {
                addNav(items, "My Dashboard",    "\u25A3");
                addNav(items, "Browse Books",    "\u25A1");
                addNav(items, "My Borrows",      "\u21C4");
                addNav(items, "My Reservations", "\u27BF");
                addNav(items, "My Fines",        "\u25C7");
                addNav(items, "Notifications",   "\u25CB");
                addNav(items, "My Profile",      "\u25D0");
            }
        }
        sb.add(items, BorderLayout.CENTER);

        // bottom — logout
        JPanel bottom = new JPanel();
        bottom.setBackground(AppTheme.sidebarBg());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 16, 10));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JPanel sep = new JPanel();
        sep.setBackground(new Color(255, 255, 255, 15));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottom.add(sep); bottom.add(Box.createVerticalStrut(10));

        NavItem logout = new NavItem("Sign Out", "\u2192", false);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { logout(); }
        });
        bottom.add(logout);
        sb.add(bottom, BorderLayout.SOUTH);
        return sb;
    }

    private void addNav(JPanel p, String name, String icon) {
        NavItem item = new NavItem(name, icon, true);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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

    /* ── Sidebar Nav Item ────────────────────────────────────────── */

    class NavItem extends JPanel {
        private final String name, icon;
        private final boolean tracked;
        private boolean hover;

        NavItem(String name, String icon, boolean tracked) {
            this.name = name; this.icon = icon; this.tracked = tracked;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(AppTheme.SIDEBAR_W - 20, 38));
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
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 5, 3, getHeight() - 10, 3, 3));
            } else if (hover) {
                g2.setColor(AppTheme.sidebarHover());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            }
            Color col = act ? AppTheme.ACCENT : hover ? Color.WHITE : new Color(0x94, 0xA3, 0xB8);
            g2.setFont(AppTheme.SIDEBAR); g2.setColor(col);
            g2.drawString(icon, 14, 24);
            g2.setColor(act ? Color.WHITE : col);
            g2.drawString(name, 38, 24);
        }
    }

    /* ── Entry point ─────────────────────────────────────────────── */

    public static void launch() {
        AppTheme.initLookAndFeel();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
