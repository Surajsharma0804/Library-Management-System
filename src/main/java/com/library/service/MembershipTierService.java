package com.library.service;

import com.library.model.LibraryConfig;
import com.library.model.MembershipTier;
import com.library.model.Student;
import com.library.repository.MembershipTierRepository;
import com.library.repository.UserRepository;
import com.library.security.AuthorizationManager;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing membership tiers and computing effective borrowing
 * limits per student.
 *
 * <p>All mutating operations require the {@link Permissions#CONFIG_UPDATE}
 * permission; read operations require {@link Permissions#CONFIG_VIEW};
 * assigning a tier to a student requires {@link Permissions#STUDENT_UPDATE}.
 *
 * <p>Requirements: 9.5, 23.1
 */
public final class MembershipTierService {

    private final MembershipTierRepository tierRepo;
    private final UserRepository userRepo;
    private final AuthorizationManager rbac;
    private final AuditService auditService;

    /**
     * Constructs a {@code MembershipTierService} with all required dependencies.
     *
     * @param tierRepo     repository for persisting {@link MembershipTier} entities
     * @param userRepo     repository for loading and persisting {@link Student} entities
     * @param rbac         authorization manager used to enforce permissions
     * @param auditService service for recording audit trail entries
     */
    public MembershipTierService(MembershipTierRepository tierRepo,
                                  UserRepository userRepo,
                                  AuthorizationManager rbac,
                                  AuditService auditService) {
        this.tierRepo = Objects.requireNonNull(tierRepo, "tierRepo must not be null");
        this.userRepo = Objects.requireNonNull(userRepo, "userRepo must not be null");
        this.rbac = Objects.requireNonNull(rbac, "rbac must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    /**
     * Creates a new membership tier and persists it.
     *
     * @param session               the authenticated session (must have {@code CONFIG_UPDATE})
     * @param tierName              non-blank display name for the tier
     * @param borrowLimit           maximum concurrent borrows; must be &ge; 1
     * @param loanPeriodDays        loan duration in days; must be &ge; 1
     * @param renewalLimit          maximum renewals allowed; must be &ge; 0
     * @param maxActiveReservations maximum simultaneous active reservations; must be &ge; 1
     * @return the newly created and persisted {@link MembershipTier}
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks {@code CONFIG_UPDATE}
     * @throws IllegalArgumentException                          if any parameter fails validation
     */
    public MembershipTier create(Session session,
                                  String tierName,
                                  int borrowLimit,
                                  int loanPeriodDays,
                                  int renewalLimit,
                                  int maxActiveReservations) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.CONFIG_UPDATE);

        validateTierFields(tierName, borrowLimit, loanPeriodDays, renewalLimit, maxActiveReservations);

        String id = UUID.randomUUID().toString();
        MembershipTier tier = MembershipTier.builder()
                .id(id)
                .tierName(tierName.trim())
                .borrowLimit(borrowLimit)
                .loanPeriodDays(loanPeriodDays)
                .renewalLimit(renewalLimit)
                .maxActiveReservations(maxActiveReservations)
                .build();

        tierRepo.save(tier);
        auditService.record(session, "TIER_CREATE", "MembershipTier", id,
                "Created tier '" + tier.getTierName() + "' borrowLimit=" + borrowLimit);
        return tier;
    }

    /**
     * Updates an existing membership tier's fields.
     *
     * @param session               the authenticated session (must have {@code CONFIG_UPDATE})
     * @param tierId                the ID of the tier to update
     * @param tierName              new non-blank display name
     * @param borrowLimit           new borrow limit; must be &ge; 1
     * @param loanPeriodDays        new loan period in days; must be &ge; 1
     * @param renewalLimit          new renewal limit; must be &ge; 0
     * @param maxActiveReservations new max active reservations; must be &ge; 1
     * @return the updated {@link MembershipTier}
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks {@code CONFIG_UPDATE}
     * @throws IllegalArgumentException                          if any parameter fails validation
     * @throws NoSuchElementException                            if no tier with the given ID exists
     */
    public MembershipTier update(Session session,
                                  String tierId,
                                  String tierName,
                                  int borrowLimit,
                                  int loanPeriodDays,
                                  int renewalLimit,
                                  int maxActiveReservations) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(tierId, "tierId must not be null");
        rbac.require(session, Permissions.CONFIG_UPDATE);

        validateTierFields(tierName, borrowLimit, loanPeriodDays, renewalLimit, maxActiveReservations);

        MembershipTier tier = tierRepo.findById(tierId)
                .orElseThrow(() -> new NoSuchElementException("Membership tier not found: " + tierId));

        tier.setTierName(tierName.trim());
        tier.setBorrowLimit(borrowLimit);
        tier.setLoanPeriodDays(loanPeriodDays);
        tier.setRenewalLimit(renewalLimit);
        tier.setMaxActiveReservations(maxActiveReservations);

        tierRepo.save(tier);
        auditService.record(session, "TIER_UPDATE", "MembershipTier", tierId,
                "Updated tier '" + tier.getTierName() + "'");
        return tier;
    }

    /**
     * Deletes a membership tier by ID.
     *
     * <p>Deletion is rejected if any student currently references this tier.
     *
     * @param session the authenticated session (must have {@code CONFIG_UPDATE})
     * @param tierId  the ID of the tier to delete
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks {@code CONFIG_UPDATE}
     * @throws NoSuchElementException                            if no tier with the given ID exists
     * @throws IllegalStateException                             if any student is currently assigned this tier
     */
    public void delete(Session session, String tierId) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(tierId, "tierId must not be null");
        rbac.require(session, Permissions.CONFIG_UPDATE);

        // Verify the tier exists before attempting to check usage or delete
        tierRepo.findById(tierId)
                .orElseThrow(() -> new NoSuchElementException("Membership tier not found: " + tierId));

        boolean inUse = userRepo.findAllStudents().stream()
                .anyMatch(s -> tierId.equals(s.getMembershipTierId()));
        if (inUse) {
            throw new IllegalStateException("Tier is in use by students and cannot be deleted.");
        }

        tierRepo.deleteById(tierId);
        auditService.record(session, "TIER_DELETE", "MembershipTier", tierId,
                "Deleted tier " + tierId);
    }

    /**
     * Returns all membership tiers.
     *
     * @param session the authenticated session (must have {@code CONFIG_VIEW})
     * @return unmodifiable list of all tiers
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks {@code CONFIG_VIEW}
     */
    public List<MembershipTier> findAll(Session session) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.CONFIG_VIEW);
        return tierRepo.findAll();
    }

    /**
     * Finds a single membership tier by ID.
     *
     * @param session the authenticated session (must have {@code CONFIG_VIEW})
     * @param tierId  the tier's primary key
     * @return an {@link Optional} containing the tier, or empty if not found
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks {@code CONFIG_VIEW}
     */
    public Optional<MembershipTier> findById(Session session, String tierId) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(tierId, "tierId must not be null");
        rbac.require(session, Permissions.CONFIG_VIEW);
        return tierRepo.findById(tierId);
    }

    /**
     * Assigns a membership tier to a student and updates the student's stored borrow limit.
     *
     * @param session            the authenticated session (must have {@code STUDENT_UPDATE})
     * @param registrationNumber the student's registration number
     * @param tierId             the ID of the tier to assign
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks {@code STUDENT_UPDATE}
     * @throws NoSuchElementException                            if no student or tier with the given IDs exists
     */
    public void assignToStudent(Session session, String registrationNumber, String tierId) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(registrationNumber, "registrationNumber must not be null");
        Objects.requireNonNull(tierId, "tierId must not be null");
        rbac.require(session, Permissions.STUDENT_UPDATE);

        Student student = userRepo.findStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            throw new NoSuchElementException("Student not found: " + registrationNumber);
        }

        MembershipTier tier = tierRepo.findById(tierId)
                .orElseThrow(() -> new NoSuchElementException("Membership tier not found: " + tierId));

        student.setMembershipTierId(tierId);
        student.setBorrowLimit(tier.getBorrowLimit());
        userRepo.save(student);

        auditService.record(session, "TIER_ASSIGN", "Student", registrationNumber,
                "Assigned tier '" + tier.getTierName() + "' to student " + registrationNumber);
    }

    /**
     * Returns the effective borrow limit for a student.
     *
     * <p>If the student has a membership tier assigned and it can be found in the
     * repository, the tier's borrow limit is returned. Otherwise the library-wide
     * default borrow limit from {@code config} is used.
     *
     * @param student the student whose borrow limit to compute
     * @param config  the current library configuration (used as fallback)
     * @return the effective borrow limit
     */
    public int effectiveBorrowLimit(Student student, LibraryConfig config) {
        Objects.requireNonNull(student, "student must not be null");
        Objects.requireNonNull(config, "config must not be null");

        String tierId = student.getMembershipTierId();
        if (tierId != null) {
            Optional<MembershipTier> tier = tierRepo.findById(tierId);
            if (tier.isPresent()) {
                return tier.get().getBorrowLimit();
            }
        }
        return config.getDefaultBorrowLimit();
    }

    /**
     * Returns the effective loan period in days for a student.
     *
     * <p>If the student has a membership tier assigned and it can be found in the
     * repository, the tier's loan period is returned. Otherwise the library-wide
     * default from {@code config} is used.
     *
     * @param student the student whose loan period to compute
     * @param config  the current library configuration (used as fallback)
     * @return the effective loan period in days
     */
    public int effectiveLoanPeriodDays(Student student, LibraryConfig config) {
        Objects.requireNonNull(student, "student must not be null");
        Objects.requireNonNull(config, "config must not be null");

        String tierId = student.getMembershipTierId();
        if (tierId != null) {
            Optional<MembershipTier> tier = tierRepo.findById(tierId);
            if (tier.isPresent()) {
                return tier.get().getLoanPeriodDays();
            }
        }
        return config.getLoanPeriodDays();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void validateTierFields(String tierName,
                                     int borrowLimit,
                                     int loanPeriodDays,
                                     int renewalLimit,
                                     int maxActiveReservations) {
        if (tierName == null || tierName.isBlank()) {
            throw new IllegalArgumentException("tierName must not be blank");
        }
        if (borrowLimit < 1) {
            throw new IllegalArgumentException("borrowLimit must be >= 1, got: " + borrowLimit);
        }
        if (loanPeriodDays < 1) {
            throw new IllegalArgumentException("loanPeriodDays must be >= 1, got: " + loanPeriodDays);
        }
        if (renewalLimit < 0) {
            throw new IllegalArgumentException("renewalLimit must be >= 0, got: " + renewalLimit);
        }
        if (maxActiveReservations < 1) {
            throw new IllegalArgumentException(
                    "maxActiveReservations must be >= 1, got: " + maxActiveReservations);
        }
    }
}
