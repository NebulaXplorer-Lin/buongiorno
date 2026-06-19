package model;

/**
 * Stores the identity of the user currently authenticated in the application.
 */
public class Session {
    String currentUserId;

    /**
     * Creates a session with no authenticated user.
     */
    public Session() {
        currentUserId = null;
    }

    /**
     * Reports whether a user is currently authenticated.
     *
     * @return {@code true} when the session contains a user ID
     */
    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    /**
     * Marks a user as authenticated.
     *
     * @param userId identifier of the authenticated user
     */
    public void login(String userId) {
        currentUserId = userId;
    }

    /**
     * Clears the authenticated user from the session.
     */
    public void logout() {
        currentUserId = null;

    }

    /**
     * Returns the authenticated user's identifier.
     *
     * @return the current user ID, or {@code null} when logged out
     */
    public String getCurrentUserId() {
        return currentUserId;
    }
}
