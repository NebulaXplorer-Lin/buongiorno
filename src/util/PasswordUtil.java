package util;

public class PasswordUtil {
    public static String hashPassword(String password) {
        return Integer.toString(password.hashCode());

    }

    public static boolean verifyPassword(String password, String passwordHash) {
        if (password == null || passwordHash == null)
            return false;

        return hashPassword(password).equals(passwordHash);
    }
}