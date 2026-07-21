package com.library.service;

import com.library.service.AuditService;
import com.library.enums.NotificationType;
import com.library.enums.FineStatus;
import com.library.exception.InvalidRegistrationException;
import com.library.factory.EntityFactory;
import com.library.model.Fine;
import com.library.model.Student;
import com.library.notification.NotificationEvent;
import com.library.notification.NotificationPublisher;
import com.library.repository.FineRepository;
import com.library.repository.UserRepository;
import com.library.security.Session;
import com.library.util.DateUtils;

import java.util.List;

/**
 * Service for fine management: recording, collecting, and waiving fines.
 */
public final class FineService {

    private final FineRepository repo;
    private final UserRepository studentRepo;
    private final EntityFactory factory;
    private final AuditService auditService;
    private final NotificationPublisher notifications;

    public FineService(FineRepository repo, UserRepository studentRepo,
                       EntityFactory factory, AuditService auditService,
                       NotificationPublisher notifications) {
        this.repo = repo;
        this.studentRepo = studentRepo;
        this.factory = factory;
        this.auditService = auditService;
        this.notifications = notifications;
    }

    /** Records a new fine and updates the student's balance. */
    public Fine recordFine(Session session, String registrationNumber, String borrowId,
                           String bookId, long amountPaise, String reason) {
        Fine fine = factory.createFine(registrationNumber, borrowId, bookId, amountPaise,
                session == null ? "system" : session.username(), reason);
        repo.save(fine);
        Student student = studentRepo.findStudentByRegistrationNumber(registrationNumber);
        if (student != null) {
            student.addFine(amountPaise);
            studentRepo.save(student);
        }
        if (session != null) {
            auditService.record(session, "FINE_RECORD", "Fine", fine.getId(),
                    "Fine of " + (amountPaise / 100.0) + " for " + reason);
        }
        notifications.publish(new NotificationEvent(
                registrationNumber, NotificationType.FINE_RECORDED, "Fine Recorded",
                "A fine of " + (amountPaise / 100.0) + " was recorded: " + reason, DateUtils.now()));
        return fine;
    }

    /** Collects (marks paid) a fine, reducing the student's balance. */
    public Fine collectFine(Session session, String fineId) {
        Fine fine = repo.findById(fineId)
                .orElseThrow(() -> new InvalidRegistrationException("Fine not found: " + fineId));
        if (fine.getStatus() != FineStatus.PENDING) {
            throw new IllegalStateException("Fine " + fineId + " is already " + fine.getStatus());
        }
        fine.setStatus(FineStatus.PAID);
        fine.setSettledBy(session.username());
        fine.setSettledAt(DateUtils.now());
        repo.save(fine);
        Student student = studentRepo.findStudentByRegistrationNumber(fine.getRegistrationNumber());
        if (student != null) {
            student.subtractFine(fine.getAmountPaise());
            studentRepo.save(student);
        }
        auditService.record(session, "FINE_COLLECT", "Fine", fineId,
                "Collected " + (fine.getAmountPaise() / 100.0) + " from " + fine.getRegistrationNumber());
        notifications.publish(new NotificationEvent(
                fine.getRegistrationNumber(), NotificationType.FINE_RECORDED, "Fine Paid",
                "Your fine of " + (fine.getAmountPaise() / 100.0) + " has been paid.", DateUtils.now()));
        return fine;
    }

    /** Waives a fine, reducing the student's balance without payment. */
    public Fine waiveFine(Session session, String fineId, String reason) {
        Fine fine = repo.findById(fineId)
                .orElseThrow(() -> new InvalidRegistrationException("Fine not found: " + fineId));
        if (fine.getStatus() != FineStatus.PENDING) {
            throw new IllegalStateException("Fine " + fineId + " is already " + fine.getStatus());
        }
        fine.setStatus(FineStatus.WAIVED);
        fine.setSettledBy(session.username());
        fine.setSettledAt(DateUtils.now());
        fine.setReason(reason);
        repo.save(fine);
        Student student = studentRepo.findStudentByRegistrationNumber(fine.getRegistrationNumber());
        if (student != null) {
            student.subtractFine(fine.getAmountPaise());
            studentRepo.save(student);
        }
        auditService.record(session, "FINE_WAIVE", "Fine", fineId,
                "Waived " + (fine.getAmountPaise() / 100.0) + " for " + fine.getRegistrationNumber()
                        + ": " + reason);
        notifications.publish(new NotificationEvent(
                fine.getRegistrationNumber(), NotificationType.FINE_RECORDED, "Fine Waived",
                "Your fine of " + (fine.getAmountPaise() / 100.0) + " was waived.", DateUtils.now()));
        return fine;
    }

    public List<Fine> findByStudent(String registrationNumber) {
        return repo.findByRegistrationNumber(registrationNumber);
    }

    public List<Fine> findPendingByStudent(String registrationNumber) {
        return repo.findPendingByRegistrationNumber(registrationNumber);
    }

    public List<Fine> findAllPending() {
        return repo.findAllPending();
    }

    public long totalPendingPaise() {
        return repo.findAllPending().stream().mapToLong(Fine::getAmountPaise).sum();
    }
}
