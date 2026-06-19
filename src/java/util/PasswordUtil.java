package util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final String ALGORITHM_NAME = "pbkdf2-sha256";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String FIELD_SEPARATOR = "$";
    private static final int ITERATIONS = 600_000;
    private static final int MIN_ACCEPTED_ITERATIONS = 100_000;
    private static final int MAX_ACCEPTED_ITERATIONS = 2_000_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int HASH_LENGTH = KEY_LENGTH_BITS / Byte.SIZE;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS);

        return String.join(
                FIELD_SEPARATOR,
                ALGORITHM_NAME,
                Integer.toString(ITERATIONS),
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash));
    }

    public static boolean verifyPassword(String password, String storedPasswordHash) {
        if (password == null || password.isEmpty()
                || storedPasswordHash == null || storedPasswordHash.isEmpty()) {
            return false;
        }

        try {
            String[] fields = storedPasswordHash.split("\\$", -1);
            if (fields.length != 4 || !ALGORITHM_NAME.equals(fields[0])) {
                return false;
            }

            int iterations = Integer.parseInt(fields[1]);
            if (iterations < MIN_ACCEPTED_ITERATIONS || iterations > MAX_ACCEPTED_ITERATIONS) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(fields[2]);
            byte[] expectedHash = Base64.getDecoder().decode(fields[3]);
            if (salt.length != SALT_LENGTH || expectedHash.length != HASH_LENGTH) {
                return false;
            }

            byte[] actualHash = pbkdf2(password, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        char[] passwordCharacters = password.toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(
                passwordCharacters,
                salt,
                iterations,
                KEY_LENGTH_BITS);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("PBKDF2 password hashing is unavailable.", exception);
        } finally {
            keySpec.clearPassword();
            Arrays.fill(passwordCharacters, '\0');
        }
    }
}
