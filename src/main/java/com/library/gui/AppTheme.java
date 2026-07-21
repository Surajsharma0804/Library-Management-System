package com.library.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Design system — provides dynamic light and dark theme colours,
 * shared fonts, and pre-styled component factories for consistent
 * look-and-feel across every panel.
 *
 * @author University Central Library — Software Engineering Division
 * @version 1.0.0
 */
public final class AppTheme {

    private AppTheme() {}

    /* ────────── Theme state ────────────────────────────────────── */

    private static boolean darkMode = true;
    private static final List<Runnable> listeners = new ArrayList<>();

    public static boolean isDark() { return darkMode; }

    public static void toggle() {
        darkMode = !darkMode;
        listeners.forEach(Runnable::run);
    }

    public static void onThemeChange(Runnable r) { listeners.add(r); }

    /* ────────── Colour palette ─────────────────────────────────── */

    // Backgrounds
    public static Color bg()          { return darkMode ? c(0x0F1419) : c(0xF5F6FA); }
    public static Color bgCard()      { return darkMode ? c(0x1A1F2B) : c(0xFFFFFF); }
    public static Color bgElevated()  { return darkMode ? c(0x232A36) : c(0xFFFFFF); }
    public static Color bgInput()     { return darkMode ? c(0x131820) : c(0xF5F6FA); }
    public static Color bgHover()     { return darkMode ? c(0x2A3140) : c(0xEEEFF2); }

    // Sidebar
    public static Color sidebarBg()      { return darkMode ? c(0x0B0F14) : c(0x1E2530); }
    public static Color sidebarHover()   { return darkMode ? c(0x1A2030) : c(0x2A3240); }
    public static Color sidebarActive()  { return darkMode ? c(0x14243D) : c(0x162340); }

    // Borders
    public static Color border()      { return darkMode ? c(0x2A303C) : c(0xDDE1E6); }
    public static Color borderFocus() { return ACCENT; }

    // Text
    public static Color fg()          { return darkMode ? c(0xECEFF4) : c(0x1A1D23); }
    public static Color fgSecondary() { return darkMode ? c(0x8892A0) : c(0x5A6270); }
    public static Color fgMuted()     { return darkMode ? c(0x565E6C) : c(0x9CA3AF); }

    // Table
    public static Color tableAlt()    { return darkMode ? c(0x141920) : c(0xF8F9FB); }
    public static Color tableHdr()    { return darkMode ? c(0x0F1419) : c(0xF0F1F4); }

    // Accent colours (constant across themes)
    public static final Color ACCENT  = c(0x3B82F6);
    public static final Color ACCENT2 = c(0x60A5FA);
    public static final Color GREEN   = c(0x22C55E);
    public static final Color AMBER   = c(0xF59E0B);
    public static final Color RED     = c(0xEF4444);
    public static final Color VIOLET  = c(0xA78BFA);
    public static final Color TEAL    = c(0x14B8A6);
    public static final Color ROSE    = c(0xF43F5E);

    /* ────────── Fonts ──────────────────────────────────────────── */

    public static final Font H1       = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font H2       = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font H3       = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BODY_B   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font SMALL_B  = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font METRIC   = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font SIDEBAR  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font INPUT    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font TABLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font TBL_HDR  = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font LOGO     = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font BTN      = new Font("Segoe UI", Font.BOLD, 14);

    /* ────────── Dimensions ─────────────────────────────────────── */

    public static final int SIDEBAR_W = 250;
    public static final int CARD_R    = 14;
    public static final int BTN_R     = 10;
    public static final int INP_R     = 8;

    /* ────────── Rendering ──────────────────────────────────────── */

    public static void aa(Graphics g) {
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /* ────────── Component factories ────────────────────────────── */

    public static JButton primaryBtn(String text) {
        var btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                Color base = getModel().isRollover() ? ACCENT2 : ACCENT;
                GradientPaint gp = new GradientPaint(0, 0, base.brighter(), 0, getHeight(), base);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BTN_R, BTN_R));
                // subtle shine
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() / 2f, BTN_R, BTN_R));
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                var fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        style(btn); return btn;
    }

    public static JButton secondaryBtn(String text) {
        var btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? bgHover() : bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BTN_R, BTN_R));
                g2.setColor(border()); g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-1, getHeight()-1, BTN_R, BTN_R));
                g2.setFont(getFont()); g2.setColor(fg());
                var fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        style(btn); return btn;
    }

    public static JButton dangerBtn(String text) {
        var btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? RED.brighter() : RED);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BTN_R, BTN_R));
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                var fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        style(btn); return btn;
    }

    private static void style(JButton b) {
        b.setFont(BTN); b.setPreferredSize(new Dimension(140, 40));
        b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JTextField textField(int cols) {
        var f = new JTextField(cols) {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(bgInput());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INP_R, INP_R));
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(hasFocus() ? borderFocus() : border());
                g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1.2f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-1, getHeight()-1, INP_R, INP_R));
            }
        };
        f.setFont(INPUT); f.setForeground(fg()); f.setCaretColor(ACCENT);
        f.setOpaque(false); f.setBorder(new EmptyBorder(8, 12, 8, 12));
        f.setPreferredSize(new Dimension(280, 42));
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        var f = new JPasswordField(cols) {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(bgInput());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INP_R, INP_R));
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(hasFocus() ? borderFocus() : border());
                g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1.2f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-1, getHeight()-1, INP_R, INP_R));
            }
        };
        f.setFont(INPUT); f.setForeground(fg()); f.setCaretColor(ACCENT);
        f.setOpaque(false); f.setBorder(new EmptyBorder(8, 12, 8, 12));
        f.setPreferredSize(new Dimension(280, 42));
        return f;
    }

    public static JComboBox<String> comboBox(String... items) {
        var cb = new JComboBox<>(items);
        cb.setFont(INPUT); cb.setBackground(bgInput()); cb.setForeground(fg());
        cb.setPreferredSize(new Dimension(280, 42));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cb.setBorder(new EmptyBorder(2, 4, 2, 4));
        return cb;
    }

    /* ────────── Labels ─────────────────────────────────────────── */

    public static JLabel label(String t)   { var l = new JLabel(t); l.setFont(BODY);  l.setForeground(fg());          return l; }
    public static JLabel label2(String t)  { var l = new JLabel(t); l.setFont(SMALL); l.setForeground(fgSecondary()); return l; }
    public static JLabel heading(String t) { var l = new JLabel(t); l.setFont(H2);    l.setForeground(fg());          return l; }

    /* ────────── Metric card ────────────────────────────────────── */

    public static JPanel metricCard(String title, String value, Color accent) {
        var card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                // shadow
                g2.setColor(new Color(0, 0, 0, darkMode ? 30 : 10));
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth()-2, getHeight()-2, CARD_R, CARD_R));
                // body
                g2.setColor(bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-2, getHeight()-2, CARD_R, CARD_R));
                if (!darkMode) {
                    g2.setColor(border()); g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-3, getHeight()-3, CARD_R, CARD_R));
                }
                // left accent bar
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight()-2, 4, 4));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 20));
        card.setPreferredSize(new Dimension(200, 120));

        var t = new JLabel(title.toUpperCase());
        t.setFont(SMALL_B); t.setForeground(fgMuted()); t.setAlignmentX(Component.LEFT_ALIGNMENT);
        var v = new JLabel(value);
        v.setFont(METRIC); v.setForeground(fg()); v.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(t); card.add(Box.createVerticalStrut(8)); card.add(v);
        return card;
    }

    /* ────────── Table styling ──────────────────────────────────── */

    public static void styleTable(JTable t) {
        t.setFont(TABLE); t.setForeground(fg()); t.setBackground(bgCard());
        t.setSelectionBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
        t.setSelectionForeground(fg());
        t.setGridColor(border()); t.setRowHeight(44);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);

        JTableHeader h = t.getTableHeader();
        h.setFont(TBL_HDR); h.setForeground(fgMuted()); h.setBackground(tableHdr());
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, border()));
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 48));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean s, boolean f, int r, int c) {
                var lbl = (JLabel) super.getTableCellRendererComponent(tbl, v, s, f, r, c);
                lbl.setFont(TBL_HDR); lbl.setForeground(fgMuted()); lbl.setBackground(tableHdr());
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, border()),
                        new EmptyBorder(0, 14, 0, 14)));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean s, boolean fc, int r, int c) {
                    var lbl = (JLabel) super.getTableCellRendererComponent(tbl, v, s, fc, r, c);
                    lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
                    lbl.setFont(TABLE);
                    return lbl;
                }
            });
        }
    }

    public static JScrollPane scroll(Component view) {
        var sp = new JScrollPane(view);
        sp.setBackground(bgCard()); sp.getViewport().setBackground(bgCard());
        sp.setBorder(BorderFactory.createLineBorder(border()));
        sp.getVerticalScrollBar().setUI(new SmoothScrollBar());
        sp.getHorizontalScrollBar().setUI(new SmoothScrollBar());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    /* ────────── Theme toggle button ────────────────────────────── */

    public static JButton themeBtn() {
        var b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? bgHover() : bgElevated());
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 8, 8);
                g2.setColor(border()); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 8, 8);
                // draw icon
                int cx = getWidth() / 2, cy = getHeight() / 2;
                if (darkMode) {
                    // sun icon
                    g2.setColor(AMBER);
                    g2.fillOval(cx - 6, cy - 6, 12, 12);
                    g2.setStroke(new BasicStroke(1.5f));
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI * 2 * i / 8;
                        g2.drawLine(cx + (int)(9*Math.cos(a)), cy + (int)(9*Math.sin(a)),
                                    cx + (int)(12*Math.cos(a)), cy + (int)(12*Math.sin(a)));
                    }
                } else {
                    // moon icon
                    g2.setColor(new Color(0x6366F1));
                    g2.fillOval(cx - 7, cy - 7, 14, 14);
                    g2.setColor(bgElevated());
                    g2.fillOval(cx - 3, cy - 9, 12, 12);
                }
            }
        };
        b.setPreferredSize(new Dimension(36, 36));
        b.setMaximumSize(new Dimension(36, 36));
        b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(darkMode ? "Switch to Light Mode" : "Switch to Dark Mode");
        b.addActionListener(e -> toggle());
        return b;
    }

    /* ────────── Dialogs ────────────────────────────────────────── */

    public static void error(Component p, String m)   { JOptionPane.showMessageDialog(p, m, "Error",   JOptionPane.ERROR_MESSAGE); }
    public static void success(Component p, String m) { JOptionPane.showMessageDialog(p, m, "Success", JOptionPane.INFORMATION_MESSAGE); }
    public static boolean confirm(Component p, String m) {
        return JOptionPane.showConfirmDialog(p, m, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /* ────────── Utilities ──────────────────────────────────────── */

    private static Color c(int rgb) { return new Color(rgb); }

    static class SmoothScrollBar extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = border(); trackColor = bgCard(); }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() { var b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            aa(g); var g2 = (Graphics2D) g;
            g2.setColor(new Color(0x48, 0x4F, 0x58, 160));
            g2.fill(new RoundRectangle2D.Float(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6));
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor); g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
