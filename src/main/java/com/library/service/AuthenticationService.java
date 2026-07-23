package com.library.service;

import com.library.config.Constants;
import com.library.enums.UserRole;
import com.library.exception.ValidationException;
import com.library.exception.UnauthorizedAccessException;
import com.library.model.Student;
import com.library.model.User;
import com.library.repository.StaffRepository;
import com.library.repository.UserRepository;
import com.library.security.Session;
import com.library.security.SessionManager;
import com.library.util.AppLogger;
import com.library.security.PasswordHasher;
import com.library.validator.BusinessValidators;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication service: verifies credentials, issues sessions, handles
 * password changes, and enforces account lockout after repeated failures.
 *
 * <p>Lockout rules (Requirements 30.1, 30.2):
 * <ul>
 *   <li>After {@link Constants#MAX_LOGIN_ATTEMPTS} consecutive failed attempts
 *       within the {@link Constants#LOCKOUT_WINDOW_MINUTES}-minute window the
 *       account is locked for {@link Constants#LOCKOUT_DURATION_MINUTES} minutes.</li>
 *   <li>A successful login clears the failure counter.</li>
 * </ul>
 */
public final class AuthenticationService
        implements com.library.interfaces.AuthenticationService {

    private static final String LOG = "AuthService";

    // ------------------------------------------------------------------ Lockout
    /**
     * Tracks consecutive failed login attempts per username.
     * Lazily evicted when the lockout window expires.
     */
    private final ConcurrentHashMap<String, FailedAttempt> failedAttempts = new ConcurrentHashMap<>();

    /**
     * Immutable snapshot of failed-attempt state for a single username.
     */
    private record FailedAttempt(int count, Instant firstFailure) {}

    // --------------------------------------------------------------- Repositories
    private final StaffRepository staffRepo;
    private final UserRepository studentRepo;
    private final SessionManager sessionManager;

    /**
     * Creates an {@code AuthenticationService}.
     *
     * @param staffRepo      repository for staff/admin users; must not be {@code null}
     * @param studentRepo    repository for student users; must not be {@code null}
     * @param sessionManager session lifecycle manager; must not be {@code null}
     */
    public AuthenticationService(StaffRepository staffRepo,
                                 UserRepository studentRepo,
                                 SessionManager sessionManager) {
        this.staffRepo      = Objects.requireNonNull(staffRepo,      "staffRepo");
        this.studentRepo    = Objects.requireNonNull(studentRepo,    "studentRepo");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Enforces account lockout: if the username has accumulated
     * {@link Constants#MAX_LOGIN_ATTEMPTS} failures within the lockout window,
     * a {@link ValidationException} is thrown with the unlock time (Req 30.1).
     * A successful authentication clears all failure counters (Req 30.2).
     */
    @Override
    public String login(String username, String password) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");

        // --- Lockout check (Req 30.1) ---
        FailedAttempt attempt = failedAttempts.get(username);
        if (attempt != null && attempt.count() >= Constants.MAX_LOGIN_ATTEMPTS) {
            Instant lockoutEnd = attempt.firstFailure()
                    .plus(Constants.LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES);
            if (Instant.now().isBefore(lockoutEnd)) {
                AppLogger.warn(LOG, "Account locked for user: " + username);
                throw new ValidationException(
                        "Account locked. Too many failed attempts. Try again after " + lockoutEnd);
            } else {
                // Lockout window expired — evict lazily
                failedAttempts.remove(username);
            }
        }

        // --- Resolve user ---
        User user = staffRepo.findByUsername(username);
        if (user == null) {
            Student student = studentRepo.findStudentByUsername(username);
            if (student != null) {
                user = student;
            }
        }
        if (user == null) {
            AppLogger.warn(LOG, "Failed login for unknown username: " + username);
            recordFailure(username);
            throw new ValidationException("Invalid username or password.");
        }
        if (!user.isActive()) {
            AppLogger.warn(LOG, "Login blocked for inactive user: " + username);
            throw new ValidationException("Account is inactive. Contact the administrator.");
        }
        if (!PasswordHasher.verify(user.getPasswordHash(), password)) {
            AppLogger.warn(LOG, "Failed login (bad password) for user: " + username);
            recordFailure(username);
            throw new ValidationException("Invalid username or password.");
        }

        // --- Success: clear failures (Req 30.2) ---
        failedAttempts.remove(username);
        Session session = sessionManager.create(user);
        AppLogger.info(LOG, "User " + username + " logged in as " + user.getRole());
        return session.token();
    }

    @Override
    public void logout(String token) {
        sessionManager.invalidate(token);
    }

    @Override
    public Session currentSession(String token) {
        return sessionManager.require(token);
    }

    /** Changes the password for the current session's user. */
    public void changePassword(String token, String oldPassword, String newPassword) {
        Session session = sessionManager.require(token);
        User user = resolveUser(session);
        if (!PasswordHasher.verify(user.getPasswordHash(), oldPassword)) {
            throw new ValidationException("Current password is incorrect.");
        }
        BusinessValidators.validatePassword(newPassword);
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        persist(user);
        AppLogger.info(LOG, "Password changed for user " + session.username());
    }

    /** Administrator/librarian resets a student's password to a temporary value. */
    public String resetStudentPassword(String registrationNumber, String tempPassword) {
        Student student = studentRepo.findStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            throw new ValidationException("No student with registration number: " + registrationNumber);
        }
        BusinessValidators.validatePassword(tempPassword);
        student.setPasswordHash(PasswordHasher.hash(tempPassword));
        studentRepo.save(student);
        AppLogger.info(LOG, "Password reset for student " + registrationNumber);
        return tempPassword;
    }

    // -------------------------------------------------------------------------
    // Lockout helpers
    // -------------------------------------------------------------------------

    /**
     * Records a failed login attempt for {@code username}, starting or advancing
     * the rolling failure window.
     */
    private void recordFailure(String username) {
        failedAttempts.compute(username, (k, existing) -> {
            if (existing == null || Instant.now().isAfter(
                    existing.firstFailure().plus(Constants.LOCKOUT_WINDOW_MINUTES, ChronoUnit.MINUTES))) {
                return new FailedAttempt(1, Instant.now());
            }
            return new FailedAttempt(existing.count() + 1, existing.firstFailure());
        });
    }

    // -------------------------------------------------------------------------
    // Persistence helpers
    // -------------------------------------------------------------------------

    private User resolveUser(Session session) {
        if (session.role() == UserRole.STUDENT) {
            Student s = studentRepo.findStudentByUsername(session.username());
            if (s == null) {
                throw new UnauthorizedAccessException("Session user no longer exists.");
            }
            return s;
        }
        return staffRepo.findById(session.userId())
                .orElseThrow(() -> new UnauthorizedAccessException("Session user no longer exists."));
    }

    private void persist(User user) {
        if (user instanceof Student s) {
            studentRepo.save(s);
        } else {
            staffRepo.save(user);
        }
    }
}
