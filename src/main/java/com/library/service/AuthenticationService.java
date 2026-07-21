package com.library.service;

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

import java.util.Objects;

/**
 * Authentication service: verifies credentials, issues sessions, and
 * handles password changes. This is the single authority on who is
 * logged in.
 */
public final class AuthenticationService
        implements com.library.interfaces.AuthenticationService {

    private static final String LOG = "AuthService";
    private final StaffRepository staffRepo;
    private final UserRepository studentRepo;
    private final SessionManager sessionManager;

    public AuthenticationService(StaffRepository staffRepo,
                                 UserRepository studentRepo,
                                 SessionManager sessionManager) {
        this.staffRepo = staffRepo;
        this.studentRepo = studentRepo;
        this.sessionManager = sessionManager;
    }

    @Override
    public String login(String username, String password) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        User user = staffRepo.findByUsername(username);
        if (user == null) {
            Student student = studentRepo.findStudentByUsername(username);
            if (student != null) {
                user = student;
            }
        }
        if (user == null) {
            AppLogger.warn(LOG, "Failed login for unknown username: " + username);
            throw new ValidationException("Invalid username or password.");
        }
        if (!user.isActive()) {
            AppLogger.warn(LOG, "Login blocked for inactive user: " + username);
            throw new ValidationException("Account is inactive. Contact the administrator.");
        }
        if (!PasswordHasher.verify(user.getPasswordHash(), password)) {
            AppLogger.warn(LOG, "Failed login (bad password) for user: " + username);
            throw new ValidationException("Invalid username or password.");
        }
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
