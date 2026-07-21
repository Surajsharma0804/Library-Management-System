package com.library.service;

import com.library.exception.ValidationException;
import com.library.model.Librarian;
import com.library.model.User;
import com.library.repository.StaffRepository;
import com.library.security.Permissions;
import com.library.security.AuthorizationManager;
import com.library.security.PasswordHasher;
import com.library.security.Session;
import com.library.security.SessionManager;

import java.util.List;
import java.util.Set;

public final class UserService {

    private final StaffRepository staffRepo;
    private final SessionManager sessionManager;
    private final AuthorizationManager rbac;
    private final AuditService auditService;

    public UserService(StaffRepository staffRepo, SessionManager sessionManager,
                      AuthorizationManager rbac, AuditService auditService) {
        this.staffRepo = staffRepo;
        this.sessionManager = sessionManager;
        this.rbac = rbac;
        this.auditService = auditService;
    }

    public List<User> getAllUsers(Session session) {
        rbac.require(session, Permissions.LIBRARIAN_VIEW);
        return staffRepo.findAll().stream().map(u -> (User) u).toList();
    }

    public User getUser(Session session, String userId) {
        rbac.require(session, Permissions.LIBRARIAN_VIEW);
        return staffRepo.findById(userId)
                .map(u -> (User) u)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));
    }

    public void activateUser(Session session, String userId) {
        rbac.require(session, Permissions.LIBRARIAN_UPDATE);
        User user = staffRepo.findById(userId)
                .map(u -> (User) u)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));
        user.setActive(true);
        staffRepo.save(user);
        auditService.record(session, "USER_ACTIVATE", "User", userId, "Activated user");
    }

    public void deactivateUser(Session session, String userId) {
        rbac.require(session, Permissions.LIBRARIAN_UPDATE);
        User user = staffRepo.findById(userId)
                .map(u -> (User) u)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));
        user.setActive(false);
        staffRepo.save(user);
        auditService.record(session, "USER_DEACTIVATE", "User", userId, "Deactivated user");
    }

    public int getActiveSessionCount(Session session) {
        rbac.require(session, Permissions.LIBRARIAN_VIEW);
        return (int) sessionManager.lookup(session.token()).map(s -> 1).orElse(0);
    }

    public Librarian addLibrarian(Session session, String firstName, String lastName, String email,
                                  String phone, String username, String password,
                                  Set<String> permissions) {
        rbac.require(session, Permissions.LIBRARIAN_ADD);
        if (staffRepo.findByUsername(username) != null) {
            throw new ValidationException("Username already exists: " + username);
        }
        Librarian lib = Librarian.builder()
                .id("lib-" + System.nanoTime())
                .firstName(firstName).lastName(lastName)
                .email(email).phone(phone)
                .username(username)
                .passwordHash(PasswordHasher.hash(password))
                .permissions(permissions)
                .build();
        staffRepo.save(lib);
        auditService.record(session, "LIBRARIAN_ADD", "Librarian", lib.getId(),
                "Added librarian " + username);
        return lib;
    }

    public Librarian updateLibrarian(Session session, Librarian librarian) {
        rbac.require(session, Permissions.LIBRARIAN_UPDATE);
        staffRepo.save(librarian);
        auditService.record(session, "LIBRARIAN_UPDATE", "Librarian", librarian.getId(),
                "Updated librarian " + librarian.getUsername());
        return librarian;
    }

    public boolean removeLibrarian(Session session, String librarianId) {
        rbac.require(session, Permissions.LIBRARIAN_REMOVE);
        boolean removed = staffRepo.deleteById(librarianId);
        if (removed) {
            auditService.record(session, "LIBRARIAN_REMOVE", "Librarian", librarianId,
                    "Removed librarian");
        }
        return removed;
    }

    public String resetPassword(Session session, String librarianId, String tempPassword) {
        rbac.require(session, Permissions.LIBRARIAN_RESET_PASSWORD);
        User user = staffRepo.findById(librarianId)
                .map(u -> (User) u)
                .orElseThrow(() -> new ValidationException("User not found: " + librarianId));
        user.setPasswordHash(PasswordHasher.hash(tempPassword));
        staffRepo.save(user);
        auditService.record(session, "LIBRARIAN_RESET_PASSWORD", "Librarian", librarianId,
                "Reset password");
        return tempPassword;
    }

    public Librarian assignPermissions(Session session, String librarianId, Set<String> permissions) {
        rbac.require(session, Permissions.LIBRARIAN_ASSIGN_PERMISSIONS);
        User user = staffRepo.findById(librarianId)
                .map(u -> (User) u)
                .orElseThrow(() -> new ValidationException("User not found: " + librarianId));
        if (user instanceof Librarian lib) {
            permissions.forEach(lib::grantPermission);
            staffRepo.save(lib);
            auditService.record(session, "LIBRARIAN_ASSIGN_PERMISSIONS", "Librarian", librarianId,
                    "Assigned " + permissions.size() + " permissions");
            return lib;
        }
        throw new ValidationException("User is not a librarian: " + librarianId);
    }

    public List<Librarian> findAll() {
        return staffRepo.findAllLibrarians();
    }
}
