package com.library.service;

import com.library.enums.BorrowStatus;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.LostBookRecord;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.LostBookRepository;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.security.AuthorizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing lost-book events.
 *
 * <p>When a borrow record is marked lost, this service:
 * <ol>
 *   <li>Creates a {@link LostBookRecord} capturing the event details.</li>
 *   <li>Calls {@link Book#markLost()} to decrement total quantity.</li>
 *   <li>Sets the {@link BorrowRecord} status to {@link BorrowStatus#LOST}.</li>
 *   <li>Creates a replacement-cost fine via {@link FineService}.</li>
 *   <li>Records an audit trail entry via {@link AuditService}.</li>
 * </ol>
 *
 * <p>Requirements: 20.1, 20.2
 */
public final class LostBookService {

    private final LostBookRepository lostBookRepo;
    private final BorrowRepository borrowRepo;
    private final BookRepository bookRepo;
    private final FineService fineService;
    private final AuditService auditService;

    /**
     * Constructs a {@code LostBookService} with all required dependencies.
     *
     * @param lostBookRepo repository for persisting {@link LostBookRecord} entities
     * @param borrowRepo   repository for loading and persisting {@link BorrowRecord} entities
     * @param bookRepo     repository for loading and persisting {@link Book} entities
     * @param fineService  service used to create the replacement-cost fine
     * @param auditService service used to record audit trail entries
     */
    public LostBookService(LostBookRepository lostBookRepo,
                            BorrowRepository borrowRepo,
                            BookRepository bookRepo,
                            FineService fineService,
                            AuditService auditService) {
        this.lostBookRepo = Objects.requireNonNull(lostBookRepo, "lostBookRepo must not be null");
        this.borrowRepo = Objects.requireNonNull(borrowRepo, "borrowRepo must not be null");
        this.bookRepo = Objects.requireNonNull(bookRepo, "bookRepo must not be null");
        this.fineService = Objects.requireNonNull(fineService, "fineService must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    /**
     * Marks a borrowed book as lost, updates all relevant records, creates a
     * replacement-cost fine, and records an audit entry.
     *
     * <p>Steps performed (in strict order):
     * <ol>
     *   <li>Load {@link BorrowRecord} by {@code borrowId}; throw
     *       {@link NoSuchElementException} if absent.</li>
     *   <li>Verify the borrow record is {@link BorrowStatus#ACTIVE}; throw
     *       {@link IllegalStateException} otherwise.</li>
     *   <li>Load {@link Book} by the record's book ID; throw
     *       {@link NoSuchElementException} if absent.</li>
     *   <li>Build a new {@link LostBookRecord}.</li>
     *   <li>Call {@link Book#markLost()} (decrements totalQuantity).</li>
     *   <li>Persist the updated book.</li>
     *   <li>Set borrow record status to {@link BorrowStatus#LOST}.</li>
     *   <li>Persist the updated borrow record.</li>
     *   <li>Persist the new lost-book record.</li>
     *   <li>Create a replacement-cost fine.</li>
     *   <li>Record an audit entry.</li>
     * </ol>
     *
     * @param session              the authenticated session performing the action
     * @param borrowId             the ID of the active borrow record
     * @param replacementCostPaise replacement cost for the lost book in paise
     * @return the newly created and persisted {@link LostBookRecord}
     * @throws NoSuchElementException if no borrow record or book is found
     * @throws IllegalStateException  if the borrow record is not in {@code ACTIVE} status
     */
    public LostBookRecord markLost(Session session, String borrowId, long replacementCostPaise) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(borrowId, "borrowId must not be null");

        // Step 1: Load borrow record
        BorrowRecord record = borrowRepo.findById(borrowId)
                .orElseThrow(() -> new NoSuchElementException("Borrow record not found: " + borrowId));

        // Step 2: Ensure it is active
        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new IllegalStateException("Borrow record is not active");
        }

        // Step 3: Load book
        Book book = bookRepo.findById(record.getBookId())
                .orElseThrow(() -> new NoSuchElementException("Book not found: " + record.getBookId()));

        // Step 4: Build LostBookRecord
        LostBookRecord lostBookRecord = LostBookRecord.builder()
                .id(UUID.randomUUID().toString())
                .borrowRecordId(borrowId)
                .bookId(record.getBookId())
                .registrationNumber(record.getRegistrationNumber())
                .replacementCostPaise(replacementCostPaise)
                .reportedDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        // Step 5: Decrement total quantity
        book.markLost();

        // Step 6: Persist book
        bookRepo.save(book);

        // Step 7: Update borrow record status
        record.setStatus(BorrowStatus.LOST);

        // Step 8: Persist borrow record
        borrowRepo.save(record);

        // Step 9: Persist lost-book record
        lostBookRepo.save(lostBookRecord);

        // Step 10: Create replacement-cost fine
        fineService.recordFine(session, record.getRegistrationNumber(), borrowId,
                record.getBookId(), replacementCostPaise, "Lost book replacement cost");

        // Step 11: Audit
        auditService.record(session, "BOOK_MARK_LOST", "LostBook", lostBookRecord.getId(),
                "Book '" + book.getTitle() + "' marked lost by " + record.getRegistrationNumber());

        // Step 12: Return
        return lostBookRecord;
    }

    /**
     * Returns all lost-book records.
     *
     * @param session the authenticated session (must have {@link Permissions#BOOK_MARK_LOST})
     * @return list of all {@link LostBookRecord} instances
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks the required permission
     */
    public List<LostBookRecord> findAll(Session session) {
        Objects.requireNonNull(session, "session must not be null");
        new AuthorizationManager().require(session, Permissions.BOOK_MARK_LOST);
        return lostBookRepo.findAll();
    }

    /**
     * Returns all lost-book records for a given student.
     *
     * @param session            the authenticated session (must have {@link Permissions#BOOK_MARK_LOST})
     * @param registrationNumber the student's registration number
     * @return list of {@link LostBookRecord} instances for the student
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks the required permission
     */
    public List<LostBookRecord> findByStudent(Session session, String registrationNumber) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(registrationNumber, "registrationNumber must not be null");
        new AuthorizationManager().require(session, Permissions.BOOK_MARK_LOST);
        return lostBookRepo.findByRegistrationNumber(registrationNumber);
    }
}
