package com.library.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Password hashing using PBKDF2-HMAC-SHA256 with a per-password random salt.
 *
 * <p>This is a self-contained Core Java implementation using only
 * {@link javax.crypto.SecretKeyFactory} - no external crypto library.
 * Stored format: {@code iterations:salt:hash} (all hex-encoded), so the
 * hash can be verified without keeping a separate salt column.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 12_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * Hashes a plain-text password.
     *
     * @return a self-describing string {@code iterations:salt:hash}
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password.toCharArray(), salt, ITERATIONS);
        return ITERATIONS + ":" + HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(hash);
    }

    /**
     * Verifies a plain-text password against a stored hash.
     *
     * @param storedHash the value previously returned by {@link #hash}
     * @param password   the candidate password
     * @return true if the password matches
     */
    public static boolean verify(String storedHash, String password) {
        if (storedHash == null || password == null) {
            return false;
        }
        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = HexFormat.of().parseHex(parts[1]);
            byte[] expected = HexFormat.of().parseHex(parts[2]);
            byte[] actual = derive(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        javax.crypto.spec.PBEKeySpec spec = null;
        try {
            spec = new javax.crypto.spec.PBEKeySpec(password, salt, iterations, HASH_BYTES * 8);
            var factory = javax.crypto.SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing unavailable: " + e.getMessage(), e);
        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

    /** SHA-256 hex digest of arbitrary text (used for non-secret fingerprints). */
    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
