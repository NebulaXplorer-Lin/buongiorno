package service;

import model.Session;
import model.SocialNetwork;
import model.User;
import util.PasswordUtil;

/**
 * Provides user registration, login, logout, and user-ID availability checks.
 */
public class AuthService {

    private SocialNetwork network;
    private Session session;

    /**
     * Creates an authentication service.
     *
     * @param network social network containing registered users
     * @param session session to update after authentication operations
     */
    public AuthService(SocialNetwork network, Session session) {
        this.network = network;
        this.session = session;
    }

    /**
     * Registers a user, hashes the supplied password, and signs the user in.
     *
     * @param userId requested user ID
     * @param password plain-text password to hash
     * @param name display name
     * @param workplace workplace
     * @param hometown hometown
     * @return {@code true} when registration succeeds; {@code false} when the
     *         user ID is already taken
     * @throws IllegalArgumentException if the password is null or empty
     */
    public boolean register(String userId, String password, String name, String workplace, String hometown) {
        if (isUserIdTaken(userId)) {
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User newUser = new User(userId, name, workplace, hometown, passwordHash);
        network.addUser(newUser);
        session.login(userId);

        return true;
    }

    /**
     * Authenticates a registered user.
     *
     * @param userId user ID
     * @param password plain-text password
     * @return {@code true} when the credentials are valid
     */
    public boolean login(String userId, String password) {
        User user = network.getUser(userId);
        if (user == null) {
            return false;
        }

        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            session.login(userId);
            return true;
        }

        return false;
    }

    /**
     * Ends the current session.
     */
    public void logout() {
        session.logout();
    }

    /**
     * Tests whether a user ID is already registered.
     *
     * @param userId user ID to test
     * @return {@code true} when the ID is present
     */
    public boolean isUserIdTaken(String userId) {
        return network.containsUser(userId);
    }
}
