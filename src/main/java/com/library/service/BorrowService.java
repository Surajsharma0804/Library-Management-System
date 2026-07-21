package com.library.service;

import com.library.service.AuditService;
import com.library.enums.BorrowStatus;
import com.library.enums.NotificationType;
import com.library.enums.ReservationStatus;
import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidRegistrationException;
import com.library.exception.ReservationException;
import com.library.factory.EntityFactory;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.LibraryConfig;
import com.library.model.Reservation;
import com.library.model.Student;
import com.library.notification.NotificationEvent;
import com.library.notification.NotificationPublisher;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.LibraryConfigRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import com.library.security.Session;
import com.library.util.DateUtils;
import com.library.validator.BusinessValidators;

import java.util.List;

/**
 * Service for circulation operations: issue, return, renew. Coordinates
 * book, student, borrow, and reservation state and fires notifications.
 */
public final class BorrowService {

    private final BookRepository bookRepo;
    private final UserRepository studentRepo;
    private final BorrowRepository borrowRepo;
    private final ReservationRepository reservationRepo;
    private final LibraryConfigRepository configRepo;
    private final EntityFactory factory;
    private final AuditService auditService;
    private final NotificationPublisher notifications;
    private final FineService fineService;

    public BorrowService(BookRepository bookRepo, UserRepository studentRepo,
                         BorrowRepository borrowRepo, ReservationRepository reservationRepo,
                         LibraryConfigRepository configRepo, EntityFactory factory,
                         AuditService auditService, NotificationPublisher notifications,
                         FineService fineService) {
        this.bookRepo = bookRepo;
        this.studentRepo = studentRepo;
        this.borrowRepo = borrowRepo;
        this.reservationRepo = reservationRepo;
        this.configRepo = configRepo;
        this.factory = factory;
        this.auditService = auditService;
        this.notifications = notifications;
        this.fineService = fineService;
    }

    public BorrowRecord issueBook(Session session, String bookId, String registrationNumber) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        Student student = studentRepo.findStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            throw new InvalidRegistrationException("No student with registration number: " + registrationNumber);
        }
        LibraryConfig config = configRepo.get();
        BusinessValidators.validateCanBorrow(student, book, config);

        BorrowRecord record = factory.createBorrow(bookId, registrationNumber,
                config.getLoanPeriodDays(), session.username());
        book.markIssued();
        bookRepo.save(book);
        student.incrementBorrowCount();
        studentRepo.save(student);
        borrowRepo.save(record);

        auditService.record(session, "BORROW_ISSUE", "Borrow", record.getId(),
                "Issued '" + book.getTitle() + "' to " + student.fullName() + " due " + record.getDueDate());
        notifications.publish(new NotificationEvent(
                registrationNumber, NotificationType.GENERAL, "Book Issued",
                "'" + book.getTitle() + "' is due on " + record.getDueDate() + ".", DateUtils.now()));
        return record;
    }

    public BorrowRecord returnBook(Session session, String borrowId) {
        BorrowRecord record = borrowRepo.findById(borrowId)
                .orElseThrow(() -> new InvalidRegistrationException("Borrow record not found: " + borrowId));
        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Borrow " + borrowId + " is not active.");
        }
        Book book = bookRepo.findById(record.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + record.getBookId()));
        Student student = studentRepo.findStudentByRegistrationNumber(record.getRegistrationNumber());
        LibraryConfig config = configRepo.get();

        java.time.LocalDate today = DateUtils.today();
        record.setReturnDate(today);
        record.setReceivedBy(session.username());

        boolean overdue = record.getDueDate().isBefore(today);
        if (overdue) {
            long overdueDays = DateUtils.daysBetween(record.getDueDate(), today);
            long finePaise = overdueDays * config.getFinePerDayPaise();
            record.setFinePaise(finePaise);
            record.setStatus(BorrowStatus.RETURNED_LATE);
            if (student != null) {
                fineService.recordFine(session, student.getRegistrationNumber(),
                        record.getId(), record.getBookId(), finePaise,
                        "Overdue by " + overdueDays + " days");
            }
        } else {
            record.setStatus(BorrowStatus.RETURNED);
        }

        book.markReturned();
        bookRepo.save(book);
        if (student != null) {
            student.decrementBorrowCount();
            studentRepo.save(student);
        }
        borrowRepo.save(record);

        auditService.record(session, "BORROW_RETURN", "Borrow", record.getId(),
                "Returned '" + book.getTitle() + "'" + (overdue ? " (late, fine applied)" : ""));
        notifications.publish(new NotificationEvent(
                record.getRegistrationNumber(), NotificationType.GENERAL, "Book Returned",
                "'" + book.getTitle() + "' returned" + (overdue ? " with a fine of "
                        + (record.getFinePaise() / 100.0) : "") + ".", DateUtils.now()));

        fulfilNextReservation(book, session);
        return record;
    }

    public BorrowRecord renewBook(Session session, String borrowId) {
        BorrowRecord record = borrowRepo.findById(borrowId)
                .orElseThrow(() -> new InvalidRegistrationException("Borrow record not found: " + borrowId));
        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Only active borrows can be renewed.");
        }
        Book book = bookRepo.findById(record.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + record.getBookId()));
        LibraryConfig config = configRepo.get();
        List<Reservation> reservations = reservationRepo.findPendingByBookId(book.getId());

        BusinessValidators.validateCanRenew(record, config, reservations);
        record.incrementRenewCount();
        record.setDueDate(DateUtils.plusDays(DateUtils.today(), config.getLoanPeriodDays()));
        borrowRepo.save(record);

        auditService.record(session, "BORROW_RENEW", "Borrow", record.getId(),
                "Renewed borrow " + borrowId + " to " + record.getDueDate());
        notifications.publish(new NotificationEvent(
                record.getRegistrationNumber(), NotificationType.GENERAL, "Book Renewed",
                "'" + book.getTitle() + "' renewed. New due date: " + record.getDueDate() + ".",
                DateUtils.now()));
        return record;
    }

    private void fulfilNextReservation(Book book, Session session) {
        List<Reservation> pending = reservationRepo.findPendingByBookId(book.getId());
        if (pending.isEmpty()) {
            return;
        }
        pending.sort(java.util.Comparator.comparingInt(Reservation::getQueuePosition));
        Reservation next = pending.get(0);
        next.setStatus(ReservationStatus.READY);
        LibraryConfig config = configRepo.get();
        next.setExpiryDate(DateUtils.plusDays(DateUtils.today(), config.getReservationHoldDays()));
        reservationRepo.save(next);
        auditService.record(session, "RESERVATION_READY", "Reservation", next.getId(),
                "Reservation ready for " + next.getRegistrationNumber() + " on '" + book.getTitle() + "'");
        notifications.publish(new NotificationEvent(
                next.getRegistrationNumber(), NotificationType.RESERVATION_READY, "Reservation Ready",
                "'" + book.getTitle() + "' is now available. Collect by " + next.getExpiryDate() + ".",
                DateUtils.now()));
    }

    public List<BorrowRecord> findActiveByStudent(String registrationNumber) {
        return borrowRepo.findActiveByRegistrationNumber(registrationNumber);
    }

    public List<BorrowRecord> findHistoryByStudent(String registrationNumber) {
        return borrowRepo.findByRegistrationNumber(registrationNumber);
    }

    public List<BorrowRecord> findAllActive() {
        return borrowRepo.findAllActive();
    }

    public List<BorrowRecord> findAllOverdue() {
        return borrowRepo.findAllOverdue();
    }

    public BorrowRecord findById(String borrowId) {
        return borrowRepo.findById(borrowId)
                .orElseThrow(() -> new InvalidRegistrationException("Borrow record not found: " + borrowId));
    }

    public long countActive() {
        return borrowRepo.findAllActive().size();
    }
}
