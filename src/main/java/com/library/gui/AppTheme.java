package com.library.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Centralised design system for the Library Management System GUI.
 * Defines colors, fonts, dimensions, and factory methods for styled components.
 */
public final class AppTheme {

    private AppTheme() {}

    // ── Color Palette ────────────────────────────────────────────────
    public static final Color BG_PRIMARY     = new Color(0x0D, 0x11, 0x17);
    public static final Color BG_SECONDARY   = new Color(0x16, 0x1B, 0x22);
    public static final Color BG_CARD        = new Color(0x21, 0x26, 0x2D);
    public static final Color BG_CARD_HOVER  = new Color(0x30, 0x36, 0x3D);
    public static final Color BG_INPUT       = new Color(0x0D, 0x11, 0x17);
    public static final Color BORDER         = new Color(0x30, 0x36, 0x3D);
    public static final Color BORDER_FOCUS   = new Color(0x58, 0xA6, 0xFF);

    public static final Color ACCENT         = new Color(0x58, 0xA6, 0xFF);
    public static final Color ACCENT_HOVER   = new Color(0x79, 0xC0, 0xFF);
    public static final Color ACCENT_DARK    = new Color(0x1F, 0x6F, 0xEB);
    public static final Color SUCCESS        = new Color(0x3F, 0xB9, 0x50);
    public static final Color WARNING        = new Color(0xD2, 0x99, 0x22);
    public static final Color DANGER         = new Color(0xF8, 0x51, 0x49);
    public static final Color PURPLE         = new Color(0xBC, 0x8C, 0xFF);
    public static final Color ORANGE         = new Color(0xF0, 0x88, 0x3E);

    public static final Color TEXT_PRIMARY   = new Color(0xF0, 0xF6, 0xFC);
    public static final Color TEXT_SECONDARY = new Color(0x8B, 0x94, 0x9E);
    public static final Color TEXT_MUTED     = new Color(0x6E, 0x76, 0x81);

    public static final Color SIDEBAR_BG     = new Color(0x01, 0x04, 0x09);
    public static final Color SIDEBAR_HOVER  = new Color(0x21, 0x26, 0x2D);
    public static final Color SIDEBAR_ACTIVE = new Color(0x16, 0x1B, 0x22);

    public static final Color TABLE_ROW_ALT  = new Color(0x16, 0x1B, 0x22);
    public static final Color TABLE_ROW_HOVER= new Color(0x1C, 0x22, 0x2A);

    // ── Fonts ────────────────────────────────────────────────────────
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON     = new Font("Segoe UI Semibold", Font.PLAIN, 14);
    public static final Font FONT_METRIC     = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_SIDEBAR    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_TABLE      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HEAD = new Font("Segoe UI Semibold", Font.PLAIN, 12);
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

    /** Creates a styled primary button (filled accent). */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                if (getModel().isRollover()) {
                    g2.setColor(ACCENT_HOVER);
                } else {
                    g2.setColor(ACCENT);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Creates a styled secondary button (outlined). */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                if (getModel().isRollover()) {
                    g2.setColor(BG_CARD_HOVER);
                } else {
                    g2.setColor(BG_CARD);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, BUTTON_ARC, BUTTON_ARC));
                g2.setFont(getFont());
                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Creates a danger button (red). */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? DANGER.brighter() : DANGER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Creates a styled text field with rounded border. */
    public static JTextField styledTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_ARC, INPUT_ARC));
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(hasFocus() ? BORDER_FOCUS : BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, INPUT_ARC, INPUT_ARC));
            }
        };
        field.setFont(FONT_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setOpaque(false);
        field.setBackground(BG_INPUT);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setPreferredSize(new Dimension(280, 40));
        return field;
    }

    /** Creates a styled password field. */
    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), INPUT_ARC, INPUT_ARC));
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(hasFocus() ? BORDER_FOCUS : BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, INPUT_ARC, INPUT_ARC));
            }
        };
        field.setFont(FONT_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setOpaque(false);
        field.setBackground(BG_INPUT);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setPreferredSize(new Dimension(280, 40));
        return field;
    }

    /** Creates a label with standard body styling. */
    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /** Creates a muted secondary label. */
    public static JLabel secondaryLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    /** Creates a heading label. */
    public static JLabel heading(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /** Creates a metric card panel. */
    public static JPanel metricCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC));
                // Accent bar at top
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 2, 2));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(CARD_PADDING + 4, CARD_PADDING, CARD_PADDING, CARD_PADDING));
        card.setPreferredSize(new Dimension(220, 130));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_METRIC);
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());
        return card;
    }

    /** Creates a styled JTable with dark theme. */
    public static JTable styledTable(Object[][] data, String[] columns) {
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? BG_SECONDARY : TABLE_ROW_ALT);
                } else {
                    c.setBackground(ACCENT_DARK);
                }
                c.setForeground(TEXT_PRIMARY);
                return c;
            }
        };
        styleTable(table);
        return table;
    }

    /** Applies dark theme to an existing table. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_SECONDARY);
        table.setSelectionBackground(ACCENT_DARK);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEAD);
        header.setForeground(TEXT_SECONDARY);
        header.setBackground(BG_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setFont(FONT_TABLE_HEAD);
                lbl.setForeground(TEXT_SECONDARY);
                lbl.setBackground(BG_PRIMARY);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    lbl.setFont(FONT_TABLE);
                    return lbl;
                }
            });
        }
    }

    /** Creates a styled scroll pane with dark scrollbars. */
    public static JScrollPane styledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_SECONDARY);
        sp.getViewport().setBackground(BG_SECONDARY);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    /** Creates a content panel with standard padding. */
    public static JPanel contentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        return panel;
    }

    /** Creates a panel that looks like a card. */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC));
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING));
        return panel;
    }

    /** Shows a styled error dialog. */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Shows a styled success dialog. */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Shows a styled confirmation dialog. */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Custom dark scrollbar UI. */
    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = BORDER;
            this.trackColor = BG_SECONDARY;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return zeroButton(); }

        @Override
        protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private JButton zeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            AppTheme.applyAntiAliasing(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0x48, 0x4F, 0x58));
            g2.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6));
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
