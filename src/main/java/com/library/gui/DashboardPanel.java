package com.library.gui;

import com.library.dto.DashboardDTO;
import com.library.facade.LibraryFacade;
import com.library.security.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Dashboard panel with role-specific metric cards and welcome message.
 */
public final class DashboardPanel extends JPanel {

    private final LibraryFacade facade;
    private JPanel cardsPanel;
    private JLabel welcomeLabel;
    private JLabel roleLabel;
    private JLabel timestampLabel;

    public DashboardPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bgPrimary());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    }

    public void refresh(Session session) {
        removeAll();

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        welcomeLabel = new JLabel("Welcome back, " + session.username());
        welcomeLabel.setFont(AppTheme.FONT_TITLE);
        welcomeLabel.setForeground(AppTheme.textPrimary());
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleLabel = new JLabel(session.role().name() + " Dashboard");
        roleLabel.setFont(AppTheme.FONT_BODY);
        roleLabel.setForeground(AppTheme.textSecondary());
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy  |  hh:mm a"));
        timestampLabel = new JLabel(time);
        timestampLabel.setFont(AppTheme.FONT_SMALL);
        timestampLabel.setForeground(AppTheme.textMuted());
        timestampLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(welcomeLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(roleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(timestampLabel);
        header.add(Box.createVerticalStrut(30));

        add(header, BorderLayout.NORTH);

        // Cards
        DashboardDTO dto = facade.dashboard().getDashboardSummary(session);

        cardsPanel = new JPanel(new GridLayout(0, 4, 16, 16));
        cardsPanel.setOpaque(false);

        cardsPanel.add(AppTheme.metricCard("TOTAL BOOKS", String.valueOf(dto.getTotalBooks()), AppTheme.ACCENT));
        cardsPanel.add(AppTheme.metricCard("AVAILABLE", String.valueOf(dto.getAvailableBooks()), AppTheme.SUCCESS));
        cardsPanel.add(AppTheme.metricCard("BORROWED", String.valueOf(dto.getBorrowedBooks()), AppTheme.WARNING));
        cardsPanel.add(AppTheme.metricCard("OVERDUE", String.valueOf(dto.getOverdueBooks()), AppTheme.DANGER));

        switch (session.role()) {
            case ADMIN -> {
                cardsPanel.add(AppTheme.metricCard("TOTAL STUDENTS", String.valueOf(dto.getTotalStudents()), AppTheme.PURPLE));
                cardsPanel.add(AppTheme.metricCard("ACTIVE STUDENTS", String.valueOf(dto.getActiveStudents()), AppTheme.SUCCESS));
                cardsPanel.add(AppTheme.metricCard("PENDING FINES", String.valueOf(dto.getPendingFines()), AppTheme.ORANGE));
                String fineAmt = String.format("\u20B9%.2f", dto.getTotalFineAmountPaise() / 100.0);
                cardsPanel.add(AppTheme.metricCard("FINE AMOUNT", fineAmt, AppTheme.DANGER));
            }
            case LIBRARIAN -> {
                cardsPanel.add(AppTheme.metricCard("RESERVATIONS", String.valueOf(dto.getPendingReservations()), AppTheme.PURPLE));
            }
            case STUDENT -> {
                cardsPanel.add(AppTheme.metricCard("MY BORROWS", String.valueOf(dto.getBooksBorrowedByCurrentUser()), AppTheme.PURPLE));
                cardsPanel.add(AppTheme.metricCard("BORROW LIMIT", String.valueOf(dto.getRemainingBorrowLimit()), AppTheme.ORANGE));
                String myFine = String.format("\u20B9%.2f", dto.getCurrentUserFinePaise() / 100.0);
                cardsPanel.add(AppTheme.metricCard("MY FINES", myFine, AppTheme.DANGER));
            }
        }

        // Wrap cards in a top-aligned container
        JPanel cardsContainer = new JPanel(new BorderLayout());
        cardsContainer.setOpaque(false);
        cardsContainer.add(cardsPanel, BorderLayout.NORTH);

        // Quick actions card
        JPanel actionsCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                AppTheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(AppTheme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.CARD_ARC, AppTheme.CARD_ARC));
            }
        };
        actionsCard.setOpaque(false);
        actionsCard.setLayout(new BoxLayout(actionsCard, BoxLayout.Y_AXIS));
        actionsCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel actionsTitle = new JLabel("System Information");
        actionsTitle.setFont(AppTheme.FONT_SUBHEADING);
        actionsTitle.setForeground(AppTheme.textPrimary());
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ver = new JLabel("Version 1.0.0  |  Java " + System.getProperty("java.version"));
        ver.setFont(AppTheme.FONT_SMALL);
        ver.setForeground(AppTheme.textMuted());
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sessionInfo = new JLabel("Session: " + session.token().substring(0, 8) + "...  |  Role: " + session.role());
        sessionInfo.setFont(AppTheme.FONT_SMALL);
        sessionInfo.setForeground(AppTheme.textMuted());
        sessionInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionsCard.add(actionsTitle);
        actionsCard.add(Box.createVerticalStrut(12));
        actionsCard.add(ver);
        actionsCard.add(Box.createVerticalStrut(4));
        actionsCard.add(sessionInfo);

        JPanel bottomPadded = new JPanel(new BorderLayout());
        bottomPadded.setOpaque(false);
        bottomPadded.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        bottomPadded.add(actionsCard, BorderLayout.NORTH);

        cardsContainer.add(bottomPadded, BorderLayout.CENTER);

        add(cardsContainer, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
