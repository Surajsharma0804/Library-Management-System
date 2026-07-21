package com.library.util;

import com.library.security.PasswordHasher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password hasher tests")
class PasswordHasherTest {

    @Test
    @DisplayName("Hash is non-null and differs from plaintext")
    void hashIsNotNull() {
        String hash = PasswordHasher.hash("MyPassword123");
        assertNotNull(hash);
        assertNotEquals("MyPassword123", hash);
        assertTrue(hash.contains(":"));
    }

    @Test
    @DisplayName("Verify accepts correct password")
    void verifyCorrect() {
        String hash = PasswordHasher.hash("Correct123");
        assertTrue(PasswordHasher.verify(hash, "Correct123"));
    }

    @Test
    @DisplayName("Verify rejects wrong password")
    void verifyWrong() {
        String hash = PasswordHasher.hash("Correct123");
        assertFalse(PasswordHasher.verify(hash, "Wrong123"));
    }

    @Test
    @DisplayName("Each hash produces a unique salt")
    void uniqueSalt() {
        String h1 = PasswordHasher.hash("SamePassword1");
        String h2 = PasswordHasher.hash("SamePassword1");
        assertNotEquals(h1, h2);
        assertTrue(PasswordHasher.verify(h1, "SamePassword1"));
        assertTrue(PasswordHasher.verify(h2, "SamePassword1"));
    }

    @Test
    @DisplayName("Null password throws")
    void nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
    }

    @Test
    @DisplayName("Verify with corrupted hash returns false")
    void corruptedHash() {
        assertFalse(PasswordHasher.verify("corrupted", "password"));
        assertFalse(PasswordHasher.verify("a:b:c:d", "password"));
        assertFalse(PasswordHasher.verify(null, "password"));
    }

    @Test
    @DisplayName("SHA-256 produces consistent hex")
    void sha256Consistent() {
        String h1 = PasswordHasher.sha256("test");
        String h2 = PasswordHasher.sha256("test");
        assertEquals(h1, h2);
        assertEquals(64, h1.length());
    }
}
