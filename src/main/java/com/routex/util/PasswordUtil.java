package com.routex.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Wraps BCrypt so passwords are never stored or compared as plain text.
 * Used by the shared Authentication controller during register/login.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Hash was malformed / empty - treat as "does not match" rather than crashing.
            return false;
        }
    }
}
