package com.library.service;

import com.library.service.AuditService;
import com.library.enums.NotificationType;
import com.library.enums.ReservationStatus;
import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidRegistrationException;
import com.library.exception.ReservationException;
import com.library.factory.EntityFactory;
import com.library.model.Book;
import com.library.model.LibraryConfig;
import com.library.model.Reservation;
import com.library.model.Student;
import com.library.notification.NotificationEvent;
import com.library.notification.NotificationPublisher;
import com.library.repository.BookRepository;
import com.library.repository.LibraryConfigRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import com.library.security.Session;
import com.library.util.DateUtils;
import com.library.validator.BusinessValidators;

import java.util.List;

/**
 * Service for book reservations: creating, cancelling, and expiring.
 */
public final class ReservationService {

    private final ReservationRepository repo;
    private final BookRepository bookRepo;
    private final UserRepository studentRepo;
    private final LibraryConfigRepository configRepo;
    private final EntityFactory factory;
    private final AuditService auditService;
    private final NotificationPublisher notifications;

    public ReservationService(ReservationRepository repo, BookRepository bookRepo,
                              UserRepository studentRepo, LibraryConfigRepository configRepo,
                              EntityFactory factory, AuditService auditService,
                              NotificationPublisher notifications) {
        this.repo = repo;
        this.bookRepo = bookRepo;
        this.studentRepo = studentRepo;
        this.configRepo = configRepo;
        this.factory = factory;
        this.auditService = auditService;
        this.notifications = notifications;
    }

    public Reservation reserve(Session session, String bookId, String registrationNumber) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        Student student = studentRepo.findStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            throw new InvalidRegistrationException("No student with registration number: " + registrationNumber);
        }
        LibraryConfig config = configRepo.get();
        List<Reservation> activeForMember = repo.findActiveByRegistrationNumber(registrationNumber);
        BusinessValidators.validateCanReserve(student, book, activeForMember, config);

        List<Reservation> pending = repo.findPendingByBookId(bookId);
        int queuePosition = pending.size() + 1;
        Reservation reservation = factory.createReservation(bookId, registrationNumber, queuePosition, 0);
        repo.save(reservation);

        auditService.record(session, "RESERVATION_CREATE", "Reservation", reservation.getId(),
                "Reserved '" + book.getTitle() + "' for " + student.fullName() + " (queue pos " + queuePosition + ")");
        notifications.publish(new NotificationEvent(
                registrationNumber, NotificationType.RESERVATION_READY, "Reservation Placed",
                "You are #" + queuePosition + " in the queue for '" + book.getTitle() + "'.",
                DateUtils.now()));
        return reservation;
    }

    public Reservation cancel(Session session, String reservationId) {
        Reservation reservation = repo.findById(reservationId)
                .orElseThrow(() -> new ReservationException("Reservation not found: " + reservationId));
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.FULFILLED) {
            throw new ReservationException("Reservation is already " + reservation.getStatus());
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        repo.save(reservation);
        reindexQueue(reservation.getBookId());
        auditService.record(session, "RESERVATION_CANCEL", "Reservation", reservationId,
                "Cancelled reservation for " + reservation.getRegistrationNumber());
        return reservation;
    }

    public List<Reservation> findByStudent(String registrationNumber) {
        return repo.findActiveByRegistrationNumber(registrationNumber);
    }

    public List<Reservation> findByBook(String bookId) {
        return repo.findByBookId(bookId);
    }

    public List<Reservation> findAllPending() {
        return repo.findAllPending();
    }

    public List<Reservation> findAll() {
        return repo.findAll();
    }

    /** Expires READY reservations whose hold period has lapsed. */
    public int expireLapsed() {
        List<Reservation> ready = repo.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.READY)
                .filter(r -> r.getExpiryDate() != null && r.getExpiryDate().isBefore(DateUtils.today()))
                .toList();
        for (Reservation r : ready) {
            r.setStatus(ReservationStatus.EXPIRED);
            repo.save(r);
            reindexQueue(r.getBookId());
            notifications.publish(new NotificationEvent(
                    r.getRegistrationNumber(), NotificationType.RESERVATION_READY, "Reservation Expired",
                    "Your reservation expired on " + r.getExpiryDate() + ".", DateUtils.now()));
        }
        return ready.size();
    }

    private void reindexQueue(String bookId) {
        List<Reservation> pending = repo.findPendingByBookId(bookId);
        pending.sort(java.util.Comparator.comparing(Reservation::getReservationDate));
        for (int i = 0; i < pending.size(); i++) {
            Reservation r = pending.get(i);
            if (r.getQueuePosition() != i + 1) {
                r.setQueuePosition(i + 1);
                repo.save(r);
            }
        }
    }
}
