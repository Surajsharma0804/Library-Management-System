package com.library.notification;

import com.library.enums.NotificationType;

import com.library.enums.ReservationStatus;
import com.library.model.Reservation;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;

import java.util.List;

/**
 * Service for sending reservation-related notifications.
 */
public final class ReservationNotifier {

    private final ReservationRepository reservationRepo;
    private final BookRepository bookRepo;
    private final NotificationPublisher notificationManager;

    public ReservationNotifier(ReservationRepository reservationRepo, BookRepository bookRepo,
                                NotificationPublisher notificationManager) {
        this.reservationRepo = reservationRepo;
        this.bookRepo = bookRepo;
        this.notificationManager = notificationManager;
    }

    public void notifyReadyReservations() {
        List<Reservation> ready = reservationRepo.findAll(
                r -> r.getStatus() == ReservationStatus.READY);
        for (Reservation res : ready) {
            var book = bookRepo.findById(res.getBookId()).orElse(null);
            String title = book != null ? book.getTitle() : "Unknown";
            notificationManager.publish(new NotificationEvent(
                    res.getRegistrationNumber(), NotificationType.RESERVATION_READY,
                    "Reservation Ready", "Your reserved book '" + title + "' is now available.",
                    java.time.LocalDateTime.now()));
        }
    }
}
