package com.library.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Lightweight chart component supporting Bar, Pie, and Line chart types.
 * Uses a factory pattern for construction and pure Java2D for rendering.
 *
 * @author University Central Library — Software Engineering Division
 * @version 1.0.0
 */
public final class ChartPanel extends JPanel {

    public enum ChartType { BAR, PIE, LINE }

    /** Tableau-inspired color palette for chart series/slices */
    private static final Color[] PALETTE = {
        new Color(0x4e79a7), new Color(0xf28e2b), new Color(0x59a14f),
        new Color(0xe15759), new Color(0x76b7b2), new Color(0xedc948),
        new Color(0xb07aa1), new Color(0xff9da7), new Color(0x9c755f),
        new Color(0xbab0ac)
    };

    private static final int MARGIN      = 50;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    private final ChartType type;
    private final String    title;
    private final String[]  labels;
    private final long[]    values;

    private ChartPanel(ChartType type, String title, String[] labels, long[] values) {
        this.type   = type;
        this.title  = title;
        this.labels = labels != null ? labels : new String[0];
        this.values = values != null ? values : new long[0];
        setPreferredSize(new Dimension(400, 300));
        setBackground(Color.WHITE);
    }

    /** Factory: bar chart */
    public static ChartPanel barChart(String title, String[] labels, long[] values) {
        return new ChartPanel(ChartType.BAR, title, labels, values);
    }

    /** Factory: pie chart */
    public static ChartPanel pieChart(String title, String[] labels, long[] values) {
        return new ChartPanel(ChartType.PIE, title, labels, values);
    }

    /** Factory: line chart */
    public static ChartPanel lineChart(String title, String[] labels, long[] values) {
        return new ChartPanel(ChartType.LINE, title, labels, values);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values == null || values.length == 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);
        switch (type) {
            case BAR  -> drawBar(g2);
            case PIE  -> drawPie(g2);
            case LINE -> drawLine(g2);
        }
        g2.dispose();
    }

    // ──────────────────────────────────────────────────────────────
    //  BAR CHART
    // ──────────────────────────────────────────────────────────────

    private void drawBar(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Title
        drawTitle(g2, w);

        // Plot area
        int plotX  = MARGIN;
        int plotY  = MARGIN + 20;                  // room for title
        int plotW  = w - MARGIN * 2;
        int plotH  = h - MARGIN * 2 - 20;

        long maxVal = maxValue();
        if (maxVal == 0) maxVal = 1;

        int n = values.length;

        // Y-axis
        g2.setColor(new Color(0xCCCCCC));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(plotX, plotY, plotX, plotY + plotH);

        // X-axis
        g2.drawLine(plotX, plotY + plotH, plotX + plotW, plotY + plotH);

        // Y-axis grid lines and tick labels
        int yTicks = 5;
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= yTicks; i++) {
            int yPos = plotY + plotH - (int) ((double) i / yTicks * plotH);
            long tickVal = (long) ((double) i / yTicks * maxVal);

            // dashed grid line
            g2.setColor(new Color(0xEEEEEE));
            g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{4f, 4f}, 0f));
            g2.drawLine(plotX + 1, yPos, plotX + plotW, yPos);

            // tick label
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(0x666666));
            String label = formatLong(tickVal);
            g2.drawString(label, plotX - fm.stringWidth(label) - 4,
                    yPos + fm.getAscent() / 2 - 1);
        }

        // Bars
        double barSlot  = (double) plotW / n;
        double barWidth = barSlot * 0.6;

        for (int i = 0; i < n; i++) {
            int barH   = (int) ((double) values[i] / maxVal * plotH);
            int barX   = (int) (plotX + barSlot * i + (barSlot - barWidth) / 2);
            int barY   = plotY + plotH - barH;

            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.setStroke(new BasicStroke(1f));
            g2.fillRect(barX, barY, (int) barWidth, barH);

            // value above bar
            g2.setFont(LABEL_FONT);
            g2.setColor(new Color(0x333333));
            String valStr = formatLong(values[i]);
            int vx = barX + ((int) barWidth - fm.stringWidth(valStr)) / 2;
            g2.drawString(valStr, vx, barY - 3);

            // X-axis label
            if (labels != null && i < labels.length) {
                g2.setColor(new Color(0x555555));
                String lbl = labels[i];
                int lx = (int) (plotX + barSlot * i + (barSlot - fm.stringWidth(lbl)) / 2);
                g2.drawString(lbl, lx, plotY + plotH + fm.getAscent() + 4);
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  PIE CHART
    // ──────────────────────────────────────────────────────────────

    private void drawPie(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Title
        drawTitle(g2, w);

        long total = 0;
        for (long v : values) total += v;
        if (total == 0) return;

        // Legend width estimate
        int legendW = 120;
        int plotX   = MARGIN;
        int plotY   = MARGIN + 20;
        int plotW   = w - MARGIN * 2 - legendW;
        int plotH   = h - MARGIN * 2 - 20;

        int diameter = Math.min(plotW, plotH);
        int cx = plotX + plotW / 2;
        int cy = plotY + plotH / 2;
        int r  = diameter / 2;

        double startAngle = 0;
        for (int i = 0; i < values.length; i++) {
            double sweep = (double) values[i] / total * 360.0;
            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fill(new Arc2D.Double(cx - r, cy - r, diameter, diameter,
                    startAngle, sweep, Arc2D.PIE));
            startAngle += sweep;
        }

        // Pie border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        startAngle = 0;
        for (long v : values) {
            double sweep = (double) v / total * 360.0;
            g2.draw(new Arc2D.Double(cx - r, cy - r, diameter, diameter,
                    startAngle, sweep, Arc2D.PIE));
            startAngle += sweep;
        }

        // Legend
        int legendX   = plotX + plotW + 8;
        int legendY   = plotY + 10;
        int swatchSz  = 12;
        int lineH     = 20;
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < values.length; i++) {
            int ly = legendY + i * lineH;
            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fillRect(legendX, ly, swatchSz, swatchSz);
            g2.setColor(new Color(0x333333));
            String lbl = (labels != null && i < labels.length) ? labels[i] : ("Series " + (i + 1));
            g2.drawString(lbl, legendX + swatchSz + 4, ly + fm.getAscent());
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  LINE CHART
    // ──────────────────────────────────────────────────────────────

    private void drawLine(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Title
        drawTitle(g2, w);

        int plotX = MARGIN;
        int plotY = MARGIN + 20;
        int plotW = w - MARGIN * 2;
        int plotH = h - MARGIN * 2 - 20;

        long maxVal = maxValue();
        if (maxVal == 0) maxVal = 1;

        int n = values.length;

        // Y-axis
        g2.setColor(new Color(0xCCCCCC));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(plotX, plotY, plotX, plotY + plotH);

        // X-axis
        g2.drawLine(plotX, plotY + plotH, plotX + plotW, plotY + plotH);

        // Y-axis ticks and grid
        int yTicks = 5;
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= yTicks; i++) {
            int yPos    = plotY + plotH - (int) ((double) i / yTicks * plotH);
            long tickVal = (long) ((double) i / yTicks * maxVal);

            g2.setColor(new Color(0xEEEEEE));
            g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{4f, 4f}, 0f));
            g2.drawLine(plotX + 1, yPos, plotX + plotW, yPos);

            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(0x666666));
            String label = formatLong(tickVal);
            g2.drawString(label, plotX - fm.stringWidth(label) - 4,
                    yPos + fm.getAscent() / 2 - 1);
        }

        if (n < 2) {
            // Single point — just draw the dot
            if (n == 1) {
                int px = plotX + plotW / 2;
                int py = plotY + plotH - (int) ((double) values[0] / maxVal * plotH);
                g2.setColor(PALETTE[0]);
                g2.fillOval(px - 4, py - 4, 8, 8);
            }
            return;
        }

        // Compute point coordinates
        int[] px = new int[n];
        int[] py = new int[n];
        double stepX = (double) plotW / (n - 1);
        for (int i = 0; i < n; i++) {
            px[i] = (int) (plotX + stepX * i);
            py[i] = plotY + plotH - (int) ((double) values[i] / maxVal * plotH);
        }

        // Draw line
        g2.setColor(PALETTE[0]);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);
        }

        // Draw filled circles at each data point
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < n; i++) {
            // white fill with colored border
            g2.setColor(Color.WHITE);
            g2.fillOval(px[i] - 4, py[i] - 4, 8, 8);
            g2.setColor(PALETTE[0]);
            g2.drawOval(px[i] - 4, py[i] - 4, 8, 8);
        }

        // X-axis labels
        g2.setColor(new Color(0x555555));
        for (int i = 0; i < n; i++) {
            if (labels != null && i < labels.length) {
                String lbl = labels[i];
                int lx = px[i] - fm.stringWidth(lbl) / 2;
                g2.drawString(lbl, lx, plotY + plotH + fm.getAscent() + 4);
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────

    private void drawTitle(Graphics2D g2, int w) {
        g2.setFont(TITLE_FONT);
        g2.setColor(new Color(0x222222));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;
        g2.drawString(title, tx, MARGIN - 10);
    }

    private long maxValue() {
        long max = 0;
        for (long v : values) if (v > max) max = v;
        return max;
    }

    /** Compact number formatting: 1000 → "1K", 1000000 → "1M" */
    private static String formatLong(long v) {
        if (v >= 1_000_000) return (v / 1_000_000) + "M";
        if (v >= 1_000)     return (v / 1_000) + "K";
        return Long.toString(v);
    }
}
