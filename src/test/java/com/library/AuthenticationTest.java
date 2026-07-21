package com.library;

import com.library.security.PasswordHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication tests")
class AuthenticationTest {

    @Test
    @DisplayName("Password hash and verify round-trip")
    void passwordHashAndVerify() {
        String password = "MySecure123";
        String hash = PasswordHasher.hash(password);
        assertNotNull(hash);
        assertTrue(PasswordHasher.verify(hash, password));
    }

    @Test
    @DisplayName("Wrong password fails verification")
    void wrongPasswordFails() {
        String hash = PasswordHasher.hash("Correct123");
        assertFalse(PasswordHasher.verify(hash, "Wrong123"));
    }

    @Test
    @DisplayName("Hash is not the plain password")
    void hashIsNotPlain() {
        String password = "Test123Pass";
        String hash = PasswordHasher.hash(password);
        assertNotEquals(password, hash);
    }
}
