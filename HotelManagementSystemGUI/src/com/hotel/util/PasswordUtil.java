package com.hotel.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil — SHA-256 password hashing helper.
 * Matches the SHA2() function used in the MySQL schema.
 */
public class PasswordUtil {

    private PasswordUtil() { /* utility class */ }

    /**
     * Returns the lowercase hex-encoded SHA-256 digest of {@code plainText}.
     *
     * @param plainText raw password
     * @return 64-character hex string
     */
    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }

    /** Returns {@code true} when {@code plainText} hashes to {@code storedHash}. */
    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equalsIgnoreCase(storedHash);
    }
}
