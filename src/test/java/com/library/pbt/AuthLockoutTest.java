package com.library.pbt;

import com.library.exception.ValidationException;
import com.library.model.Administrator;
import com.library.model.User;
import com.library.repository.StaffRepository;
import com.library.repository.UserRepository;
import com.library.security.PasswordHasher;
import com.library.security.SessionManager;
import com.library.service.AuthenticationService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/**
 * Property-based test: Account lockout behavior.
 *
 * - Fewer than 5 failed attempts: login throws ValidationException but NOT with "locked"
 * - Exactly 5 failures: subsequent attempt throws ValidationException with "locked"
 *
 * **Validates: Requirements 30.1, 30.2** (account lockout)
 */
class AuthLockoutTest {

    /** Builds a fresh AuthenticationService backed by a temp StaffRepository. */
    private AuthenticationService buildService(Path tempDir, String username, String password) {
        StaffRepository staffRepo = new StaffRepository();
        staffRepo.setOverrideFile(tempDir.resolve("staff.json"));

        User admin = Administrator.builder()
                .id("ADMIN-001")
                .username(username)
                .firstName("Test")
                .lastName("Admin")
                .passwordHash(PasswordHasher.hash(password))
                .active(true)
                .build();
        staffRepo.save(admin);

        UserRepository studentRepo = new UserRepository();
        studentRepo.setOverrideFile(tempDir.resolve("students.json"));

        SessionManager sessionManager = new SessionManager();
        return new AuthenticationService(staffRepo, studentRepo, sessionManager);
    }

    @Property(tries = 10)
    void fewerThanFiveFailsDoesNotLockAccount() throws Exception {
        // Use a unique temp dir per try via a local temp path
        Path tmpDir = java.nio.file.Files.createTempDirectory("auth-lockout-test-");
        tmpDir.toFile().deleteOnExit();

        final String username = "testadmin";
        final String correctPassword = "Correct@123";
        final String wrongPassword = "wrong-password";

        AuthenticationService service = buildService(tmpDir, username, correctPassword);

        // Perform 1-4 failed attempts (any number < MAX_LOGIN_ATTEMPTS=5)
        int failAttempts = 4;
        for (int i = 0; i < failAttempts; i++) {
            try {
                service.login(username, wrongPassword);
                assert false : "Login with wrong password should have thrown";
            } catch (ValidationException e) {
                String msg = e.getMessage().toLowerCase();
                assert !msg.contains("locked") :
                        "Account should not be locked after only " + (i + 1) + " failed attempts. Message: " + e.getMessage();
            }
        }

        // Clean up temp dir
        deleteDir(tmpDir.toFile());
    }

    @Property(tries = 10)
    void exactlyFiveFailsLocksAccount() throws Exception {
        Path tmpDir = java.nio.file.Files.createTempDirectory("auth-lockout-5-test-");
        tmpDir.toFile().deleteOnExit();

        final String username = "locktest";
        final String correctPassword = "Correct@123";
        final String wrongPassword = "wrong-password";

        AuthenticationService service = buildService(tmpDir, username, correctPassword);

        // Perform exactly 5 failed attempts
        for (int i = 0; i < 5; i++) {
            try {
                service.login(username, wrongPassword);
                assert false : "Login with wrong password should have thrown";
            } catch (ValidationException e) {
                // expected
            }
        }

        // 6th attempt should throw with "locked" in message
        try {
            service.login(username, wrongPassword);
            assert false : "Account should be locked after 5 failed attempts";
        } catch (ValidationException e) {
            String msg = e.getMessage().toLowerCase();
            assert msg.contains("locked") :
                    "Exception message should contain 'locked' after 5 failures. Got: " + e.getMessage();
        }

        deleteDir(tmpDir.toFile());
    }

    private void deleteDir(java.io.File dir) {
        if (dir.isDirectory()) {
            java.io.File[] children = dir.listFiles();
            if (children != null) {
                for (java.io.File child : children) {
                    deleteDir(child);
                }
            }
        }
        dir.delete();
    }
}
