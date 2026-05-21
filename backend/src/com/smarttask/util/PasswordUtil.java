package com.smarttask.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple password hashing utility using SHA-256 + salt.
 * Beginner-friendly and easy to explain in interviews.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hash password with random salt.
     * Stored format: salt:hash
     */
    public static String hashPassword(String plainPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        String hash = sha256(plainPassword + Base64.getEncoder().encodeToString(salt));
        return Base64.getEncoder().encodeToString(salt) + ":" + hash;
    }

    /**
     * Verify plain password against stored salted hash.
     */
    public static boolean verifyPassword(String plainPassword, String storedValue) {
        if (storedValue == null || !storedValue.contains(":")) {
            return false;
        }

        String[] parts = storedValue.split(":", 2);
        String salt = parts[0];
        String expectedHash = parts[1];

        String actualHash = sha256(plainPassword + salt);
        return actualHash.equals(expectedHash);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
