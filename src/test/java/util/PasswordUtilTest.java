package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void samePasswordProducesDifferentHashesBecauseOfRandomSalts() {
        String firstHash = PasswordUtil.hashPassword("correct horse battery staple");
        String secondHash = PasswordUtil.hashPassword("correct horse battery staple");

        assertNotEquals(firstHash, secondHash);
        assertTrue(PasswordUtil.verifyPassword("correct horse battery staple", firstHash));
        assertTrue(PasswordUtil.verifyPassword("correct horse battery staple", secondHash));
    }

    @Test
    void correctPasswordSucceedsAndWrongPasswordFails() {
        String storedHash = PasswordUtil.hashPassword("a secure password");

        assertTrue(PasswordUtil.verifyPassword("a secure password", storedHash));
        assertFalse(PasswordUtil.verifyPassword("the wrong password", storedHash));
    }

    @Test
    void storedHashContainsAlgorithmIterationsSaltAndHash() {
        String storedHash = PasswordUtil.hashPassword("format test password");
        String[] fields = storedHash.split("\\$", -1);

        assertEquals(4, fields.length);
        assertEquals("pbkdf2-sha256", fields[0]);
        assertEquals("600000", fields[1]);
        assertEquals(16, Base64.getDecoder().decode(fields[2]).length);
        assertEquals(32, Base64.getDecoder().decode(fields[3]).length);
    }

    @Test
    void invalidInputsAndMalformedHashesFailSafely() {
        String validHash = PasswordUtil.hashPassword("valid password");
        String[] fields = validHash.split("\\$", -1);

        assertFalse(PasswordUtil.verifyPassword(null, validHash));
        assertFalse(PasswordUtil.verifyPassword("", validHash));
        assertFalse(PasswordUtil.verifyPassword("valid password", null));
        assertFalse(PasswordUtil.verifyPassword("valid password", ""));
        assertFalse(PasswordUtil.verifyPassword("valid password", "97"));
        assertFalse(PasswordUtil.verifyPassword("valid password", "pbkdf2-sha256$not-a-number$x$y"));
        assertFalse(PasswordUtil.verifyPassword(
                "valid password",
                "pbkdf2-sha256$999999999$" + fields[2] + "$" + fields[3]));
        assertFalse(PasswordUtil.verifyPassword(
                "valid password",
                "pbkdf2-sha256$600000$not-base64!$" + fields[3]));
        assertFalse(PasswordUtil.verifyPassword(
                "valid password",
                "pbkdf2-sha256$600000$" + fields[2] + "$AA=="));
    }

    @Test
    void hashingNullOrEmptyPasswordIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));
    }
}
