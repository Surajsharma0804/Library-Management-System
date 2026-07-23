package com.library.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

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
 * Executive Design System — Provides FlatLaf integration, slate/zinc color tokens,
 * typography scale, and clean component factories for an enterprise-grade GUI.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class AppTheme {

    private AppTheme() {}

    /* ────────── Theme state & LookAndFeel ────────────────────────── */

    private static boolean darkMode = true;
    private static final List<Runnable> listeners = new ArrayList<>();

    public static boolean isDark() { return darkMode; }

    public static void initLookAndFeel() {
        try {
            if (darkMode) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 6);
        } catch (Exception e) {
            // fallback gracefully
        }
    }

    public static void toggle() {
        darkMode = !darkMode;
        initLookAndFeel();
        listeners.forEach(Runnable::run);
    }

    public static void onThemeChange(Runnable r) { listeners.add(r); }

    /* ────────── Executive Colour Palette ─────────────────────────── */

    // Backgrounds
    public static Color bg()          { return darkMode ? c(0x121417) : c(0xF8FAFC); }
    public static Color bgCard()      { return darkMode ? c(0x1A1D21) : c(0xFFFFFF); }
    public static Color bgElevated()  { return darkMode ? c(0x22262C) : c(0xF1F5F9); }
    public static Color bgInput()     { return darkMode ? c(0x16181C) : c(0xFFFFFF); }
    public static Color bgHover()     { return darkMode ? c(0x2A2E36) : c(0xE2E8F0); }

    // Sidebar (Always Dark Slate for Professional Contrast)
    public static Color sidebarBg()      { return c(0x0E1013); }
    public static Color sidebarHover()   { return c(0x1E2229); }
    public static Color sidebarActive()  { return c(0x1E293B); }

    // Borders
    public static Color border()      { return darkMode ? c(0x2D3239) : c(0xE2E8F0); }
    public static Color borderFocus() { return ACCENT; }

    // Text
    public static Color fg()          { return darkMode ? c(0xEDEFEF) : c(0x0F172A); }
    public static Color fgSecondary() { return darkMode ? c(0x94A3B8) : c(0x475569); }
    public static Color fgMuted()     { return darkMode ? c(0x64748B) : c(0x94A3B8); }

    // Table
    public static Color tableAlt()    { return darkMode ? c(0x17191D) : c(0xF8FAFC); }
    public static Color tableHdr()    { return darkMode ? c(0x141619) : c(0xF1F5F9); }

    // Accents (Executive Sober Tones)
    public static final Color ACCENT  = c(0x2563EB); // Royal Blue
    public static final Color ACCENT2 = c(0x3B82F6);
    public static final Color GREEN   = c(0x16A34A); // Emerald
    public static final Color AMBER   = c(0xD97706); // Warm Amber
    public static final Color RED     = c(0xDC2626); // Crimson Red
    public static final Color VIOLET  = c(0x7C3AED); // Indigo
    public static final Color TEAL    = c(0x0D9488); // Deep Teal
    public static final Color ROSE    = c(0xE11D48); // Executive Rose

    /* ────────── Typography Scale ────────────────────────────────── */

    public static final Font H1       = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font H2       = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font H3       = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BODY_B   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font SMALL_B  = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font METRIC   = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font SIDEBAR  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font INPUT    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font TABLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font TBL_HDR  = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font LOGO     = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font BTN      = new Font("Segoe UI", Font.BOLD, 13);

    /* ────────── Dimensions ─────────────────────────────────────── */

    public static final int SIDEBAR_W = 240;
    public static final int CARD_R    = 10;
    public static final int BTN_R     = 8;
    public static final int INP_R     = 8;

    /* ────────── Rendering Hints ────────────────────────────────── */

    public static void aa(Graphics g) {
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /* ────────── Component Factories ────────────────────────────── */

    public static JButton primaryBtn(String text) {
        var btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                Color bgCol = getModel().isPressed() ? ACCENT.darker() : getModel().isRollover() ? ACCENT2 : ACCENT;
                g2.setColor(bgCol);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BTN_R, BTN_R));
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
                g2.setColor(border()); g2.setStroke(new BasicStroke(1f));
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
        b.setFont(BTN); b.setPreferredSize(new Dimension(130, 38));
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
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-1, getHeight()-1, INP_R, INP_R));
            }
        };
        f.setFont(INPUT); f.setForeground(fg()); f.setCaretColor(ACCENT);
        f.setOpaque(false); f.setBorder(new EmptyBorder(8, 12, 8, 12));
        f.setPreferredSize(new Dimension(260, 38));
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
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-1, getHeight()-1, INP_R, INP_R));
            }
        };
        f.setFont(INPUT); f.setForeground(fg()); f.setCaretColor(ACCENT);
        f.setOpaque(false); f.setBorder(new EmptyBorder(8, 12, 8, 12));
        f.setPreferredSize(new Dimension(260, 38));
        return f;
    }

    public static JComboBox<String> comboBox(String... items) {
        var cb = new JComboBox<>(items);
        cb.setFont(INPUT); cb.setBackground(bgInput()); cb.setForeground(fg());
        cb.setPreferredSize(new Dimension(260, 38));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cb.setBorder(new EmptyBorder(2, 4, 2, 4));
        return cb;
    }

    /* ────────── Status Pill Component ───────────────────────────── */

    public static JPanel createStatusPill(String statusText) {
        String st = statusText != null ? statusText.toUpperCase() : "UNKNOWN";
        Color textCol;
        Color bgCol;

        switch (st) {
            case "AVAILABLE", "PAID", "ACTIVE", "RETURNED", "SUCCESS" -> {
                textCol = isDark() ? c(0x4ADE80) : c(0x166534);
                bgCol = isDark() ? new Color(0x16, 0x65, 0x34, 50) : new Color(0xDC, 0xFC, 0xE7);
            }
            case "BORROWED", "PENDING", "RESERVED", "ISSUED", "WARNING" -> {
                textCol = isDark() ? c(0xFBBF24) : c(0x92400E);
                bgCol = isDark() ? new Color(0x92, 0x40, 0x0E, 50) : new Color(0xFE, 0xF3, 0xC7);
            }
            case "OVERDUE", "UNPAID", "LOST", "CANCELLED", "ERROR" -> {
                textCol = isDark() ? c(0xF87171) : c(0x991B1B);
                bgCol = isDark() ? new Color(0x99, 0x1B, 0x1B, 50) : new Color(0xFE, 0xE2, 0xE2);
            }
            default -> {
                textCol = fgSecondary();
                bgCol = isDark() ? new Color(0x33, 0x41, 0x55, 60) : new Color(0xF1, 0xF5, 0xF9);
            }
        }

        JPanel pill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(bgCol);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            }
        };
        pill.setOpaque(false);
        pill.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 3));
        JLabel l = new JLabel(st);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(textCol);
        pill.add(l);
        return pill;
    }

    /* ────────── Labels ─────────────────────────────────────────── */

    public static JLabel label(String t)   { var l = new JLabel(t); l.setFont(BODY);  l.setForeground(fg());          return l; }
    public static JLabel label2(String t)  { var l = new JLabel(t); l.setFont(SMALL); l.setForeground(fgSecondary()); return l; }
    public static JLabel heading(String t) { var l = new JLabel(t); l.setFont(H2);    l.setForeground(fg());          return l; }

    /* ────────── Metric Card ────────────────────────────────────── */

    public static JPanel metricCard(String title, String value, Color accent) {
        return metricCard(title, value, null, accent);
    }

    public static JPanel metricCard(String title, String value, String subtext, Color accent) {
        var card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                // body
                g2.setColor(bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_R, CARD_R));
                // subtle border
                g2.setColor(border()); g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth()-1, getHeight()-1, CARD_R, CARD_R));
                // left accent strip
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), CARD_R, CARD_R));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 16));
        card.setPreferredSize(new Dimension(210, 105));

        var t = new JLabel(title.toUpperCase());
        t.setFont(SMALL_B); t.setForeground(fgMuted()); t.setAlignmentX(Component.LEFT_ALIGNMENT);
        var v = new JLabel(value);
        v.setFont(METRIC); v.setForeground(fg()); v.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(t); card.add(Box.createVerticalStrut(4)); card.add(v);

        if (subtext != null && !subtext.isEmpty()) {
            var s = new JLabel(subtext);
            s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            s.setForeground(fgSecondary()); s.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(4)); card.add(s);
        }

        return card;
    }

    /* ────────── Table Styling ──────────────────────────────────── */

    public static void styleTable(JTable t) {
        t.setFont(TABLE); t.setForeground(fg()); t.setBackground(bgCard());
        t.setSelectionBackground(isDark() ? new Color(0x3B, 0x82, 0xF6, 50) : new Color(0xDB, 0xEA, 0xFE));
        t.setSelectionForeground(fg());
        t.setGridColor(border()); t.setRowHeight(40);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);

        JTableHeader h = t.getTableHeader();
        h.setFont(TBL_HDR); h.setForeground(fgMuted()); h.setBackground(tableHdr());
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border()));
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 42));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v, boolean s, boolean f, int r, int c) {
                var lbl = (JLabel) super.getTableCellRendererComponent(tbl, v, s, f, r, c);
                lbl.setFont(TBL_HDR); lbl.setForeground(fgMuted()); lbl.setBackground(tableHdr());
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, border()),
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
                    lbl.setForeground(fg());
                    lbl.setBackground(s ? tbl.getSelectionBackground() : (r % 2 == 0 ? bgCard() : tableAlt()));
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

    /* ────────── Theme Toggle Button ────────────────────────────── */

    public static JButton themeBtn() {
        var b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                aa(g); var g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? bgHover() : bgElevated());
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 6, 6);
                g2.setColor(border()); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 6, 6);
                // draw icon
                int cx = getWidth() / 2, cy = getHeight() / 2;
                if (darkMode) {
                    // sun icon
                    g2.setColor(AMBER);
                    g2.fillOval(cx - 5, cy - 5, 10, 10);
                    g2.setStroke(new BasicStroke(1.2f));
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI * 2 * i / 8;
                        g2.drawLine(cx + (int)(8*Math.cos(a)), cy + (int)(8*Math.sin(a)),
                                    cx + (int)(11*Math.cos(a)), cy + (int)(11*Math.sin(a)));
                    }
                } else {
                    // moon icon
                    g2.setColor(new Color(0x47, 0x55, 0x69));
                    g2.fillOval(cx - 6, cy - 6, 12, 12);
                    g2.setColor(bgElevated());
                    g2.fillOval(cx - 2, cy - 8, 10, 10);
                }
            }
        };
        b.setPreferredSize(new Dimension(34, 34));
        b.setMaximumSize(new Dimension(34, 34));
        b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(darkMode ? "Switch to Light Mode" : "Switch to Dark Mode");
        b.addActionListener(e -> toggle());
        return b;
    }

    /* ────────── Dialog Helpers ──────────────────────────────────── */

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
            g2.setColor(darkMode ? new Color(0x52, 0x52, 0x5B, 180) : new Color(0x94, 0xA3, 0xB8, 180));
            g2.fill(new RoundRectangle2D.Float(r.x+2, r.y+2, r.width-4, r.height-4, 4, 4));
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor); g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
