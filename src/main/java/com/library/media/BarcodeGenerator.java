package com.library.media;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates Code-39 barcode images for library media items.
 *
 * <p>Code-39 encodes 44 characters. Each character is represented by 5 bars
 * and 4 spaces (9 elements total), where each element is either narrow (2px)
 * or wide (5px), alternating bar/space starting with a bar. A narrow
 * inter-character gap separates consecutive characters. The barcode is
 * automatically wrapped with the standard '*' start/stop sentinel.
 *
 * <p>Requirements: 17.1, 17.2, 17.7
 */
public final class BarcodeGenerator {

    private static final int NARROW = 2;
    private static final int WIDE   = 5;
    private static final int HEIGHT = 80;
    private static final int MIN_WIDTH = 300;
    /** Narrow space between encoded characters. */
    private static final int INTER_CHAR_GAP = NARROW;

    /**
     * Code-39 encoding table.
     * Key   = character to encode
     * Value = 9-bit pattern string ('1' = wide, '0' = narrow),
     *         alternating bar/space starting with bar (index 0).
     */
    private static final Map<Character, String> ENCODING = new HashMap<>(50);

    static {
        ENCODING.put('0', "000110100");
        ENCODING.put('1', "100100001");
        ENCODING.put('2', "001100001");
        ENCODING.put('3', "101100000");
        ENCODING.put('4', "000110001");
        ENCODING.put('5', "100110000");
        ENCODING.put('6', "001110000");
        ENCODING.put('7', "000100101");
        ENCODING.put('8', "100100100");
        ENCODING.put('9', "001100100");
        ENCODING.put('A', "100001001");
        ENCODING.put('B', "001001001");
        ENCODING.put('C', "101001000");
        ENCODING.put('D', "000011001");
        ENCODING.put('E', "100011000");
        ENCODING.put('F', "001011000");
        ENCODING.put('G', "000001101");
        ENCODING.put('H', "100001100");
        ENCODING.put('I', "001001100");
        ENCODING.put('J', "000011100");
        ENCODING.put('K', "100000011");
        ENCODING.put('L', "001000011");
        ENCODING.put('M', "101000010");
        ENCODING.put('N', "000010011");
        ENCODING.put('O', "100010010");
        ENCODING.put('P', "001010010");
        ENCODING.put('Q', "000000111");
        ENCODING.put('R', "100000110");
        ENCODING.put('S', "001000110");
        ENCODING.put('T', "000010110");
        ENCODING.put('U', "110000001");
        ENCODING.put('V', "011000001");
        ENCODING.put('W', "111000000");
        ENCODING.put('X', "010010001");
        ENCODING.put('Y', "110010000");
        ENCODING.put('Z', "011010000");
        ENCODING.put('-', "010000101");
        ENCODING.put('.', "110000100");
        ENCODING.put(' ', "011000100");
        ENCODING.put('*', "010010100"); // start/stop sentinel
        ENCODING.put('$', "010101000");
        ENCODING.put('/', "010100010");
        ENCODING.put('+', "010001010");
        ENCODING.put('%', "000101010");
    }

    /**
     * Generates a Code-39 barcode image for the given value.
     *
     * <p>The value is converted to upper-case before encoding and automatically
     * wrapped with the '*' start/stop sentinel. The resulting image contains the
     * barcode bars at the top and the human-readable text centred below.
     *
     * @param value text to encode; must be 1–20 characters drawn exclusively from
     *              the Code-39 character set (digits, A–Z, space and the special
     *              characters {@code - . $ / + %})
     * @return {@link BufferedImage} containing the rendered barcode
     * @throws IllegalArgumentException if {@code value} is {@code null}, blank,
     *         longer than 20 characters, or contains a character that is not in
     *         the Code-39 character set
     */
    public BufferedImage generate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Barcode value must not be null or empty");
        }

        String upper = value.toUpperCase();

        if (upper.length() > 20) {
            throw new IllegalArgumentException("Barcode value must not exceed 20 characters");
        }

        for (char c : upper.toCharArray()) {
            if (!ENCODING.containsKey(c)) {
                throw new IllegalArgumentException("Unsupported character in barcode: " + c);
            }
        }

        // Build the full encoded sequence: *<value>*
        String sequence = "*" + upper + "*";

        // Calculate total pixel width of all encoded bars + inter-character gaps
        int totalWidth = 0;
        for (char c : sequence.toCharArray()) {
            String pattern = ENCODING.get(c);
            for (char bit : pattern.toCharArray()) {
                totalWidth += (bit == '1') ? WIDE : NARROW;
            }
            totalWidth += INTER_CHAR_GAP; // gap after each character
        }
        // Remove the trailing inter-character gap (there is none after the last char)
        totalWidth -= INTER_CHAR_GAP;

        int padding    = 10;
        int imgWidth   = Math.max(MIN_WIDTH, totalWidth + 2 * padding);
        int textHeight = 20;
        int imgHeight  = HEIGHT + textHeight + 10;

        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgWidth, imgHeight);

        // Centre the barcode horizontally
        int x = (imgWidth - totalWidth) / 2;
        int y = 5;

        // Draw bars (even indices in pattern = bars, odd = spaces/background)
        for (int ci = 0; ci < sequence.length(); ci++) {
            char c = sequence.charAt(ci);
            String pattern = ENCODING.get(c);
            for (int i = 0; i < pattern.length(); i++) {
                boolean wide = (pattern.charAt(i) == '1');
                int w = wide ? WIDE : NARROW;
                if (i % 2 == 0) { // bar (black)
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, w, HEIGHT);
                }
                // space (white) — no fill needed, background is already white
                x += w;
            }
            // Inter-character gap (skip after the final character)
            if (ci < sequence.length() - 1) {
                x += INTER_CHAR_GAP;
            }
        }

        // Human-readable text centred below the barcode
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(upper);
        int textX = (imgWidth - textWidth) / 2;
        g.drawString(upper, textX, HEIGHT + textHeight);

        g.dispose();
        return img;
    }
}
