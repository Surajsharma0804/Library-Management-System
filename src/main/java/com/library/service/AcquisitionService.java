package com.library.service;

import com.library.enums.AcquisitionStatus;
import com.library.enums.UserRole;
import com.library.model.Acquisition;
import com.library.repository.AcquisitionRepository;
import com.library.security.AuthorizationManager;
import com.library.security.Permissions;
import com.library.security.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing book acquisition requests.
 *
 * <p>Lifecycle: {@code PENDING} → {@code APPROVED} or {@code REJECTED} → {@code RECEIVED}.
 *
 * <p>Requirements: 22.1, 22.2
 */
public final class AcquisitionService {

    private final AcquisitionRepository acquisitionRepo;
    private final AuthorizationManager rbac;
    private final AuditService auditService;

    /**
     * Constructs an {@code AcquisitionService} with all required dependencies.
     *
     * @param acquisitionRepo repository for acquisition request persistence
     * @param rbac            authorization manager for permission enforcement
     * @param auditService    service for recording audit trail entries
     */
    public AcquisitionService(AcquisitionRepository acquisitionRepo,
                               AuthorizationManager rbac,
                               AuditService auditService) {
        this.acquisitionRepo = Objects.requireNonNull(acquisitionRepo,
                "acquisitionRepo must not be null");
        this.rbac = Objects.requireNonNull(rbac, "rbac must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    /**
     * Submits a new book acquisition request with {@link AcquisitionStatus#PENDING} status.
     *
     * @param session              the authenticated session; must have
     *                             {@link Permissions#ACQUISITION_REQUEST}
     * @param requestedTitle       title of the requested book; must not be blank
     * @param author               author of the requested book; may be {@code null}
     * @param isbn                 ISBN of the requested book; may be {@code null}
     * @param quantity             number of copies requested; must be &gt;= 1
     * @param estimatedCostPaise   estimated per-copy cost in paise; must be &gt;= 0
     * @return the newly created and persisted {@link Acquisition}
     * @throws IllegalArgumentException if {@code requestedTitle} is blank or
     *                                  {@code quantity} is less than 1
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                                  {@link Permissions#ACQUISITION_REQUEST}
     */
    public Acquisition submitRequest(Session session,
                                     String requestedTitle,
                                     String author,
                                     String isbn,
                                     int quantity,
                                     long estimatedCostPaise) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.ACQUISITION_REQUEST);

        if (requestedTitle == null || requestedTitle.isBlank()) {
            throw new IllegalArgumentException("requestedTitle must not be blank");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }

        Acquisition acquisition = Acquisition.builder()
                .id(UUID.randomUUID().toString())
                .requestedBy(session.username())
                .requestedTitle(requestedTitle)
                .author(author)
                .isbn(isbn)
                .quantity(quantity)
                .estimatedCostPaise(estimatedCostPaise)
                .status(AcquisitionStatus.PENDING)
                .requestedDate(LocalDate.now())
                .build();
        acquisitionRepo.save(acquisition);
        return acquisition;
    }

    /**
     * Approves a pending acquisition request.
     *
     * @param session       the authenticated session; must have
     *                      {@link Permissions#ACQUISITION_APPROVE}
     * @param acquisitionId the ID of the acquisition to approve; must not be {@code null}
     * @param reviewerNotes optional reviewer notes; may be {@code null}
     * @return the updated and persisted {@link Acquisition}
     * @throws NoSuchElementException  if no acquisition with the given ID exists
     * @throws IllegalStateException   if the acquisition is not in {@link AcquisitionStatus#PENDING}
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                                 {@link Permissions#ACQUISITION_APPROVE}
     */
    public Acquisition approve(Session session, String acquisitionId, String reviewerNotes) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(acquisitionId, "acquisitionId must not be null");
        rbac.require(session, Permissions.ACQUISITION_APPROVE);

        Acquisition acquisition = acquisitionRepo.findById(acquisitionId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Acquisition not found: " + acquisitionId));

        if (acquisition.getStatus() != AcquisitionStatus.PENDING) {
            throw new IllegalStateException(
                    "Acquisition must be PENDING to approve, current status: "
                            + acquisition.getStatus());
        }

        acquisition.setStatus(AcquisitionStatus.APPROVED);
        acquisition.setReviewerNotes(reviewerNotes);
        acquisitionRepo.save(acquisition);
        auditService.record(session, "ACQUISITION_APPROVE", "Acquisition",
                acquisitionId, "Approved by " + session.username());
        return acquisition;
    }

    /**
     * Rejects a pending acquisition request.
     *
     * @param session       the authenticated session; must have
     *                      {@link Permissions#ACQUISITION_APPROVE}
     * @param acquisitionId the ID of the acquisition to reject; must not be {@code null}
     * @param reviewerNotes optional reviewer notes; may be {@code null}
     * @return the updated and persisted {@link Acquisition}
     * @throws NoSuchElementException  if no acquisition with the given ID exists
     * @throws IllegalStateException   if the acquisition is not in {@link AcquisitionStatus#PENDING}
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                                 {@link Permissions#ACQUISITION_APPROVE}
     */
    public Acquisition reject(Session session, String acquisitionId, String reviewerNotes) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(acquisitionId, "acquisitionId must not be null");
        rbac.require(session, Permissions.ACQUISITION_APPROVE);

        Acquisition acquisition = acquisitionRepo.findById(acquisitionId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Acquisition not found: " + acquisitionId));

        if (acquisition.getStatus() != AcquisitionStatus.PENDING) {
            throw new IllegalStateException(
                    "Acquisition must be PENDING to reject, current status: "
                            + acquisition.getStatus());
        }

        acquisition.setStatus(AcquisitionStatus.REJECTED);
        acquisition.setReviewerNotes(reviewerNotes);
        acquisitionRepo.save(acquisition);
        auditService.record(session, "ACQUISITION_REJECT", "Acquisition",
                acquisitionId, "Rejected by " + session.username());
        return acquisition;
    }

    /**
     * Marks an approved acquisition as received.
     *
     * @param session       the authenticated session; must have
     *                      {@link Permissions#ACQUISITION_APPROVE}
     * @param acquisitionId the ID of the acquisition to mark received; must not be {@code null}
     * @return the updated and persisted {@link Acquisition}
     * @throws NoSuchElementException  if no acquisition with the given ID exists
     * @throws IllegalStateException   if the acquisition is not in {@link AcquisitionStatus#APPROVED}
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                                 {@link Permissions#ACQUISITION_APPROVE}
     */
    public Acquisition markReceived(Session session, String acquisitionId) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(acquisitionId, "acquisitionId must not be null");
        rbac.require(session, Permissions.ACQUISITION_APPROVE);

        Acquisition acquisition = acquisitionRepo.findById(acquisitionId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Acquisition not found: " + acquisitionId));

        if (acquisition.getStatus() != AcquisitionStatus.APPROVED) {
            throw new IllegalStateException(
                    "Acquisition must be APPROVED to mark received, current status: "
                            + acquisition.getStatus());
        }

        acquisition.setStatus(AcquisitionStatus.RECEIVED);
        acquisitionRepo.save(acquisition);
        auditService.record(session, "ACQUISITION_RECEIVED", "Acquisition",
                acquisitionId, "Received by " + session.username());
        return acquisition;
    }

    /**
     * Returns acquisition requests visible to the current session user.
     *
     * <ul>
     *   <li>ADMIN sees all requests.</li>
     *   <li>LIBRARIAN sees only their own requests.</li>
     * </ul>
     *
     * @param session the authenticated session; must have
     *                {@link Permissions#ACQUISITION_REQUEST}
     * @return list of {@link Acquisition}s visible to the session user
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                {@link Permissions#ACQUISITION_REQUEST}
     */
    public List<Acquisition> findAllForSession(Session session) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.ACQUISITION_REQUEST);
        if (session.role() == UserRole.ADMIN) {
            return acquisitionRepo.findAll();
        }
        return acquisitionRepo.findByRequestedBy(session.username());
    }

    /**
     * Returns acquisition requests filtered by status.
     *
     * @param session the authenticated session; must have
     *                {@link Permissions#ACQUISITION_APPROVE}
     * @param status  the status to filter by; must not be {@code null}
     * @return list of {@link Acquisition}s with the given status
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                {@link Permissions#ACQUISITION_APPROVE}
     */
    public List<Acquisition> findByStatus(Session session, AcquisitionStatus status) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(status, "status must not be null");
        rbac.require(session, Permissions.ACQUISITION_APPROVE);
        return acquisitionRepo.findByStatus(status);
    }
}
