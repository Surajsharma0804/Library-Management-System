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
 * Centralised design system with Light/Dark mode support.
 */
public final class AppTheme {

    private AppTheme() {}

    // ── Theme State ──────────────────────────────────────────────────
    private static boolean darkMode = true;
    private static final List<Runnable> themeListeners = new ArrayList<>();

    public static boolean isDarkMode() { return darkMode; }

    public static void toggleTheme() {
        darkMode = !darkMode;
        themeListeners.forEach(Runnable::run);
    }

    public static void addThemeListener(Runnable listener) {
        themeListeners.add(listener);
    }

    // ── Dynamic Colors (change with theme) ───────────────────────────
    public static Color bgPrimary()     { return darkMode ? new Color(0x0D, 0x11, 0x17) : new Color(0xF6, 0xF8, 0xFA); }
    public static Color bgSecondary()   { return darkMode ? new Color(0x16, 0x1B, 0x22) : new Color(0xFF, 0xFF, 0xFF); }
    public static Color bgCard()        { return darkMode ? new Color(0x21, 0x26, 0x2D) : new Color(0xFF, 0xFF, 0xFF); }
    public static Color bgCardHover()   { return darkMode ? new Color(0x30, 0x36, 0x3D) : new Color(0xF0, 0xF0, 0xF0); }
    public static Color bgInput()       { return darkMode ? new Color(0x0D, 0x11, 0x17) : new Color(0xFF, 0xFF, 0xFF); }
    public static Color border()        { return darkMode ? new Color(0x30, 0x36, 0x3D) : new Color(0xD0, 0xD7, 0xDE); }
    public static Color borderFocus()   { return ACCENT; }
    public static Color textPrimary()   { return darkMode ? new Color(0xF0, 0xF6, 0xFC) : new Color(0x1F, 0x23, 0x28); }
    public static Color textSecondary() { return darkMode ? new Color(0x8B, 0x94, 0x9E) : new Color(0x65, 0x6D, 0x76); }
    public static Color textMuted()     { return darkMode ? new Color(0x6E, 0x76, 0x81) : new Color(0x8B, 0x94, 0x9E); }
    public static Color sidebarBg()     { return darkMode ? new Color(0x01, 0x04, 0x09) : new Color(0x24, 0x29, 0x2E); }
    public static Color sidebarHover()  { return darkMode ? new Color(0x21, 0x26, 0x2D) : new Color(0x32, 0x38, 0x3F); }
    public static Color sidebarActive() { return darkMode ? new Color(0x16, 0x1B, 0x22) : new Color(0x3A, 0x41, 0x49); }
    public static Color tableRowAlt()   { return darkMode ? new Color(0x16, 0x1B, 0x22) : new Color(0xF6, 0xF8, 0xFA); }
    public static Color tableRowHover() { return darkMode ? new Color(0x1C, 0x22, 0x2A) : new Color(0xEA, 0xEC, 0xEF); }

    // ── Static Accent Colors ─────────────────────────────────────────
    public static final Color ACCENT       = new Color(0x58, 0xA6, 0xFF);
    public static final Color ACCENT_HOVER = new Color(0x79, 0xC0, 0xFF);
    public static final Color ACCENT_DARK  = new Color(0x1F, 0x6F, 0xEB);
    public static final Color SUCCESS      = new Color(0x3F, 0xB9, 0x50);
    public static final Color WARNING      = new Color(0xD2, 0x99, 0x22);
    public static final Color DANGER       = new Color(0xF8, 0x51, 0x49);
    public static final Color PURPLE       = new Color(0xBC, 0x8C, 0xFF);
    public static final Color ORANGE       = new Color(0xF0, 0x88, 0x3E);

    // ── Fonts ────────────────────────────────────────────────────────
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON     = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_METRIC     = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_SIDEBAR    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_TABLE      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HEAD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_INPUT      = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LOGO       = new Font("Segoe UI", Font.BOLD, 22);

    // ── Dimensions ───────────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH = 240;
    public static final int CARD_ARC      = 16;
    public static final int BUTTON_ARC    = 10;
    public static final int INPUT_ARC     = 8;
    public static final int CARD_PADDING  = 20;

    // ── Rendering Hints ──────────────────────────────────────────────
    public static void applyAntiAliasing(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    // ── Component Factories ──────────────────────────────────────────

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        styleButton(btn);
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? bgCardHover() : bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));
                g2.setColor(border()); g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, BUTTON_ARC, BUTTON_ARC));
                g2.setFont(getFont()); g2.setColor(textPrimary());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        styleButton(btn);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? DANGER.brighter() : DANGER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        styleButton(btn);
        return btn;
    }

    private static void styleButton(JButton btn) {
        btn.setFont(FONT_BUTTON);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JTextField styledTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(bgInput());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_ARC, INPUT_ARC));
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(hasFocus() ? borderFocus() : border());
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, INPUT_ARC, INPUT_ARC));
            }
        };
        field.setFont(FONT_INPUT); field.setForeground(textPrimary()); field.setCaretColor(ACCENT);
        field.setOpaque(false); field.setBackground(bgInput());
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setPreferredSize(new Dimension(280, 40));
        return field;
    }

    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns) {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(bgInput());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_ARC, INPUT_ARC));
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(hasFocus() ? borderFocus() : border());
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, INPUT_ARC, INPUT_ARC));
            }
        };
        field.setFont(FONT_INPUT); field.setForeground(textPrimary()); field.setCaretColor(ACCENT);
        field.setOpaque(false); field.setBackground(bgInput());
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setPreferredSize(new Dimension(280, 40));
        return field;
    }

    /** Creates a styled combo box. */
    public static JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_INPUT);
        combo.setBackground(bgInput());
        combo.setForeground(textPrimary());
        combo.setPreferredSize(new Dimension(280, 40));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return combo;
    }

    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY); lbl.setForeground(textPrimary());
        return lbl;
    }
    public static JLabel secondaryLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL); lbl.setForeground(textSecondary());
        return lbl;
    }
    public static JLabel heading(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING); lbl.setForeground(textPrimary());
        return lbl;
    }

    /** Creates a metric card panel. */
    public static JPanel metricCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC));
                if (!darkMode) {
                    g2.setColor(border());
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, CARD_ARC, CARD_ARC));
                }
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 2, 2));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(CARD_PADDING + 4, CARD_PADDING, CARD_PADDING, CARD_PADDING));
        card.setPreferredSize(new Dimension(220, 130));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL); titleLabel.setForeground(textSecondary());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_METRIC); valueLabel.setForeground(textPrimary());
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel); card.add(Box.createVerticalStrut(8));
        card.add(valueLabel); card.add(Box.createVerticalGlue());
        return card;
    }

    /** Applies dark/light theme to a table. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setForeground(textPrimary());
        table.setBackground(bgSecondary());
        table.setSelectionBackground(ACCENT_DARK);
        table.setSelectionForeground(textPrimary());
        table.setGridColor(border());
        table.setRowHeight(40);
        table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEAD); header.setForeground(textSecondary());
        header.setBackground(bgPrimary());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border()));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setFont(FONT_TABLE_HEAD); lbl.setForeground(textSecondary()); lbl.setBackground(bgPrimary());
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, border()),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    lbl.setFont(FONT_TABLE);
                    return lbl;
                }
            });
        }
    }

    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(bgSecondary());
        sp.getViewport().setBackground(bgSecondary());
        sp.setBorder(BorderFactory.createLineBorder(border()));
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static JPanel contentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(bgPrimary());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        return panel;
    }

    public static JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC));
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING));
        return panel;
    }

    /** Creates a theme toggle button (sun/moon icon). */
    public static JButton themeToggleButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                // Circle bg
                g2.setColor(getModel().isRollover() ? bgCardHover() : bgCard());
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                // Icon
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2.setColor(textPrimary());
                String icon = darkMode ? "\u2600" : "\u263D";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setMaximumSize(new Dimension(36, 36));
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Toggle Light/Dark Mode");
        btn.addActionListener(e -> toggleTheme());
        return btn;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = border(); this.trackColor = bgSecondary();
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
        private JButton zeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            applyAntiAliasing(g);
            ((Graphics2D) g).setColor(new Color(0x48, 0x4F, 0x58));
            ((Graphics2D) g).fill(new RoundRectangle2D.Float(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6));
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor); g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
