package com.library.service;

import com.library.model.Branch;
import com.library.repository.BranchRepository;
import com.library.security.AuthorizationManager;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing library branch locations.
 *
 * <p>Requirements: 26.1
 */
public final class BranchService {

    private final BranchRepository branchRepo;
    private final AuthorizationManager rbac;
    private final AuditService auditService;

    /**
     * Constructs a {@code BranchService} with all required dependencies.
     *
     * @param branchRepo   repository for branch persistence
     * @param rbac         authorization manager for permission enforcement
     * @param auditService service for recording audit trail entries
     */
    public BranchService(BranchRepository branchRepo,
                          AuthorizationManager rbac,
                          AuditService auditService) {
        this.branchRepo = Objects.requireNonNull(branchRepo, "branchRepo must not be null");
        this.rbac = Objects.requireNonNull(rbac, "rbac must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    /**
     * Creates a new library branch.
     *
     * @param session    the authenticated session; must have
     *                   {@link Permissions#CONFIG_UPDATE}
     * @param branchName display name of the branch; must not be blank
     * @param location   physical location or address; may be {@code null}
     * @param phone      contact phone number; may be {@code null}
     * @return the newly created and persisted {@link Branch}
     * @throws IllegalArgumentException if {@code branchName} is blank
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                   {@link Permissions#CONFIG_UPDATE}
     */
    public Branch create(Session session, String branchName, String location, String phone) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.CONFIG_UPDATE);

        if (branchName == null || branchName.isBlank()) {
            throw new IllegalArgumentException("branchName must not be blank");
        }

        Branch branch = Branch.builder()
                .id(UUID.randomUUID().toString())
                .branchName(branchName)
                .location(location)
                .phone(phone)
                .build();
        branchRepo.save(branch);
        auditService.record(session, "BRANCH_CREATE", "Branch",
                branch.getId(), "Branch '" + branchName + "' created by " + session.username());
        return branch;
    }

    /**
     * Updates an existing library branch.
     *
     * @param session    the authenticated session; must have
     *                   {@link Permissions#CONFIG_UPDATE}
     * @param branchId   the ID of the branch to update; must not be {@code null}
     * @param branchName new display name; must not be blank
     * @param location   new physical location; may be {@code null}
     * @param phone      new contact phone number; may be {@code null}
     * @return the updated and persisted {@link Branch}
     * @throws NoSuchElementException   if no branch with the given ID exists
     * @throws IllegalArgumentException if {@code branchName} is blank
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                   {@link Permissions#CONFIG_UPDATE}
     */
    public Branch update(Session session,
                         String branchId,
                         String branchName,
                         String location,
                         String phone) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(branchId, "branchId must not be null");
        rbac.require(session, Permissions.CONFIG_UPDATE);

        if (branchName == null || branchName.isBlank()) {
            throw new IllegalArgumentException("branchName must not be blank");
        }

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new NoSuchElementException("Branch not found: " + branchId));

        branch.setBranchName(branchName);
        branch.setLocation(location);
        branch.setPhone(phone);
        branchRepo.save(branch);
        auditService.record(session, "BRANCH_UPDATE", "Branch",
                branchId, "Branch updated by " + session.username());
        return branch;
    }

    /**
     * Returns all library branches.
     *
     * @param session the authenticated session; must have
     *                {@link Permissions#CONFIG_VIEW}
     * @return list of all {@link Branch} entities
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *                {@link Permissions#CONFIG_VIEW}
     */
    public List<Branch> findAll(Session session) {
        Objects.requireNonNull(session, "session must not be null");
        rbac.require(session, Permissions.CONFIG_VIEW);
        return branchRepo.findAll();
    }

    /**
     * Looks up a branch by its ID. No authentication is required for this lookup.
     *
     * @param branchId the branch ID to look up; must not be {@code null}
     * @return an {@link Optional} containing the {@link Branch} if found,
     *         or {@link Optional#empty()} if absent
     */
    public Optional<Branch> findById(String branchId) {
        Objects.requireNonNull(branchId, "branchId must not be null");
        return branchRepo.findById(branchId);
    }
}
