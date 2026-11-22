package com.abhijai.banking.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class PasswordHasher {
    private static final int ITERATIONS = 600_000;
    private static final int KEY_BYTES = 32;
    private static final String FORMAT = "pbkdf2-sha256";
    private final SecureRandom random = new SecureRandom();

    public String hash(char[] password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] key = derive(password, salt, ITERATIONS);
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            return String.join("$", FORMAT, Integer.toString(ITERATIONS),
                    encoder.encodeToString(salt), encoder.encodeToString(key));
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    public boolean matches(char[] password, String encoded) {
        String[] fields = encoded.split("\\$", -1);
        if (fields.length != 4 || !FORMAT.equals(fields[0])) {
            return false;
        }
        try {
            int rounds = Integer.parseInt(fields[1]);
            byte[] salt = Base64.getDecoder().decode(fields[2]);
            byte[] expected = Base64.getDecoder().decode(fields[3]);
            if (rounds < 1 || rounds > 2_000_000 || salt.length != 16 || expected.length != KEY_BYTES) {
                return false;
            }
            byte[] actual = derive(password, salt, rounds);
            try {
                return MessageDigest.isEqual(expected, actual);
            } finally {
                Arrays.fill(actual, (byte) 0);
            }
        } catch (IllegalArgumentException invalidEncoding) {
            return false;
        }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BYTES * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("Password hashing is unavailable in this Java installation.", error);
        } finally {
            spec.clearPassword();
        }
    }
}
