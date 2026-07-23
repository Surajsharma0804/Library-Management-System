package com.library.service;

import com.library.enums.ILLDirection;
import com.library.enums.ILLStatus;
import com.library.model.InterLibraryLoan;
import com.library.repository.ILLRepository;
import com.library.security.AuthorizationManager;
import com.library.security.Permissions;
import com.library.security.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing Inter-Library Loan (ILL) transactions.
 *
 * <p>Supports creating loans, updating their lifecycle status, and listing all loans.
 *
 * <p>Requirements: 25.1
 */
public final class ILLService {

    private final ILLRepository illRepo;
    private final AuthorizationManager rbac;
    private final AuditService auditService;

    /**
     * Constructs an {@code ILLService} with all required dependencies.
     *
     * @param illRepo      repository for ILL persistence
     * @param rbac         authorization manager for permission enforcement
     * @param auditService service for recording audit trail entries
     */
    public ILLService(ILLRepository illRepo,
                      AuthorizationManager rbac,
                      AuditService auditService) {
        this.illRepo      = Objects.requireNonNull(illRepo,      "illRepo must not be null");
        this.rbac         = Objects.requireNonNull(rbac,         "rbac must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    /**
     * Creates a new Inter-Library Loan request with {@link ILLStatus#REQUESTED} status.
     *
     * @param session            the authenticated session; must have
     *                           {@link Permissions#ILL_MANAGE}
     * @param direction          direction of the loan (INBOUND or OUTBOUND); must not be
     *                           {@code null}
     * @param partnerLibraryName name of the partner library; must not be blank
     * @param bookTitle          title of the book being loaned; must not be blank
     * @param bookIsbn           ISBN of the book; may be {@code null}
     * @param requestedBy        identifier of the requester; may be {@code null}
     * @param expectedReturnDate expected return date; may be {@code null}
     * @param notes              optional notes; may be {@code null}
     * @return the newly created and persisted {@link InterLibraryLoan}
     * @throws IllegalArgumentException if {@code partnerLibraryName} or {@code bookTitle}
     *                                  is blank, or {@code direction} is {@code null}
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                                  {@link Permissions#ILL_MANAGE}
     */
    public InterLibraryLoan create(Session session,
                                   ILLDirection direction,
                                   String partnerLibraryName,
                                   String bookTitle,
                                   String bookIsbn,
                                   String requestedBy,
                                   LocalDate expectedReturnDate,
                                   String notes) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.ILL_MANAGE);

        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }
        if (partnerLibraryName == null || partnerLibraryName.isBlank()) {
            throw new IllegalArgumentException("partnerLibraryName must not be blank");
        }
        if (bookTitle == null || bookTitle.isBlank()) {
            throw new IllegalArgumentException("bookTitle must not be blank");
        }

        InterLibraryLoan ill = InterLibraryLoan.builder()
                .id(UUID.randomUUID().toString())
                .direction(direction)
                .partnerLibraryName(partnerLibraryName)
                .bookTitle(bookTitle)
                .bookIsbn(bookIsbn)
                .requestedBy(requestedBy)
                .expectedReturnDate(expectedReturnDate)
                .notes(notes)
                .status(ILLStatus.REQUESTED)
                .build();

        illRepo.save(ill);
        auditService.record(session, "ILL_CREATE", "InterLibraryLoan", ill.getId(),
                "Created ILL for book '" + bookTitle + "' with partner '" + partnerLibraryName + "'");
        return ill;
    }

    /**
     * Updates the status of an existing ILL and optionally updates its notes.
     *
     * <p>If {@code newStatus} is {@link ILLStatus#RETURNED}, the actual return date
     * is set to today.
     *
     * @param session       the authenticated session; must have
     *                      {@link Permissions#ILL_MANAGE}
     * @param illId         the ID of the ILL to update; must not be {@code null}
     * @param newStatus     the new lifecycle status; must not be {@code null}
     * @param notes         optional notes to attach to the ILL; may be {@code null}
     * @return the updated and persisted {@link InterLibraryLoan}
     * @throws NoSuchElementException if no ILL with the given ID exists
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                                {@link Permissions#ILL_MANAGE}
     */
    public InterLibraryLoan updateStatus(Session session, String illId,
                                          ILLStatus newStatus, String notes) {
        Objects.requireNonNull(session,   "session must not be null");
        Objects.requireNonNull(illId,     "illId must not be null");
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        rbac.require(session, Permissions.ILL_MANAGE);

        InterLibraryLoan ill = illRepo.findById(illId)
                .orElseThrow(() -> new NoSuchElementException("ILL not found: " + illId));

        ill.setStatus(newStatus);
        if (notes != null) {
            ill.setNotes(notes);
        }
        if (newStatus == ILLStatus.RETURNED) {
            ill.setActualReturnDate(LocalDate.now());
        }

        illRepo.save(ill);
        auditService.record(session, "ILL_STATUS_UPDATE", "InterLibraryLoan", illId,
                "Status changed to " + newStatus + " by " + session.username());
        return ill;
    }

    /**
     * Returns all Inter-Library Loan records.
     *
     * @param session the authenticated session; must have
     *                {@link Permissions#ILL_MANAGE}
     * @return unmodifiable list of all {@link InterLibraryLoan}s
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                {@link Permissions#ILL_MANAGE}
     */
    public List<InterLibraryLoan> findAll(Session session) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.ILL_MANAGE);
        return illRepo.findAll();
    }
}
