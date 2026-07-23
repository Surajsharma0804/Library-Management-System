package com.library.media;

import com.library.config.Constants;
import com.library.model.LibraryConfig;
import com.library.model.Student;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates student ID card images and displays a non-modal preview.
 *
 * <p>Renders a 640×380 card with a header band, avatar placeholder,
 * student details, Code-39 barcode, and a footer. The card is saved as a PNG
 * under {@code exports/cards/{registrationNumber}_card.png} and a non-modal
 * {@link JFrame} preview is opened on the Swing EDT.
 *
 * <p>Requirements: 17.3, 17.4, 17.5, 17.6
 */
public final class IDCardGenerator {

    private static final int WIDTH          = 640;
    private static final int HEIGHT         = 380;
    private static final int BARCODE_WIDTH  = 200;
    private static final int BARCODE_HEIGHT = 60;

    private final BarcodeGenerator barcodeGen = new BarcodeGenerator();

    /**
     * Renders a student ID card, saves it to {@code exports/cards/{regNum}_card.png},
     * opens a non-modal {@link JFrame} preview, and returns the saved {@link Path}.
     *
     * @param student the student whose details appear on the card; must not be {@code null}
     * @param config  library configuration (used for the library name); must not be {@code null}
     * @return the {@link Path} of the saved PNG file
     * @throws IOException if the output directory cannot be created or the file cannot be written
     */
    public Path generateAndSave(Student student, LibraryConfig config) throws IOException {
        // 1. Create the card image (640×380 white background)
        BufferedImage card = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = card.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Header: library name banner (dark blue background, white text)
        g.setColor(new Color(0x2c3e50));
        g.fillRect(0, 0, WIDTH, 60);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        String libName = (config.getLibraryName() != null && !config.getLibraryName().isBlank())
                ? config.getLibraryName()
                : Constants.LIBRARY_NAME;
        g.drawString(libName, 20, 40);

        // Sub-header: "STUDENT LIBRARY CARD"
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("STUDENT LIBRARY CARD", WIDTH - 180, 40);

        // Divider line (accent blue)
        g.setColor(new Color(0x3498db));
        g.fillRect(0, 60, WIDTH, 4);

        // Left side: avatar placeholder (light grey rectangle with "?" icon)
        g.setColor(new Color(0xecf0f1));
        g.fillRect(20, 80, 120, 150);
        g.setColor(new Color(0xbdc3c7));
        g.setFont(new Font("SansSerif", Font.PLAIN, 40));
        g.drawString("?", 60, 170);

        // Right side: student info
        int infoX = 160;
        int infoY = 100;
        int lineH  = 28;

        g.setColor(new Color(0x2c3e50));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(student.getFirstName() + " " + student.getLastName(), infoX, infoY);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        infoY += lineH;
        g.setColor(new Color(0x7f8c8d));
        g.drawString("Reg No:", infoX, infoY);
        g.setColor(new Color(0x2c3e50));
        g.drawString(student.getRegistrationNumber(), infoX + 80, infoY);

        infoY += lineH;
        g.setColor(new Color(0x7f8c8d));
        g.drawString("Dept:", infoX, infoY);
        g.setColor(new Color(0x2c3e50));
        String dept = student.getDepartment() != null ? student.getDepartment() : "\u2014";
        g.drawString(dept, infoX + 80, infoY);

        infoY += lineH;
        g.setColor(new Color(0x7f8c8d));
        g.drawString("Expires:", infoX, infoY);
        g.setColor(new Color(0x2c3e50));
        String expiry = student.getMembershipExpiry() != null
                ? student.getMembershipExpiry().toString()
                : "\u2014";
        g.drawString(expiry, infoX + 80, infoY);

        // Barcode — generate from registrationNumber, strip non-Code-39 chars
        String barcodeValue = student.getRegistrationNumber()
                .replaceAll("[^A-Z0-9 \\-\\.\\$\\/\\+\\%]", "")
                .toUpperCase();
        if (!barcodeValue.isEmpty() && barcodeValue.length() <= 20) {
            try {
                BufferedImage barcode = barcodeGen.generate(barcodeValue);
                int bx = (WIDTH - BARCODE_WIDTH) / 2;
                int by = HEIGHT - BARCODE_HEIGHT - 50;
                g.drawImage(barcode, bx, by, BARCODE_WIDTH, BARCODE_HEIGHT, null);
            } catch (Exception ignored) {
                // Skip barcode rendering if encoding fails for any reason
            }
        }

        // Footer strip
        g.setColor(new Color(0xecf0f1));
        g.fillRect(0, HEIGHT - 30, WIDTH, 30);
        g.setColor(new Color(0x7f8c8d));
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("This card is property of " + libName + ". If found, please return.", 20, HEIGHT - 10);

        g.dispose();

        // 2. Save to exports/cards/{regNum}_card.png
        Path cardsDir = Path.of(Constants.CARDS_EXPORT_DIR);
        Files.createDirectories(cardsDir);
        String filename = student.getRegistrationNumber().replaceAll("[^A-Za-z0-9_\\-]", "_") + "_card.png";
        Path output = cardsDir.resolve(filename);
        ImageIO.write(card, "PNG", output.toFile());

        // 3. Open non-modal JFrame preview (on EDT)
        final BufferedImage finalCard = card; // effectively final for lambda
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ID Card \u2014 " + student.getFirstName() + " " + student.getLastName());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setResizable(false);
            JLabel label = new JLabel(new ImageIcon(finalCard));
            frame.add(label);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        return output;
    }
}
