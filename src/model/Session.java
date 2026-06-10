package model;

public class Session {
    String currentUserId;

    public Session() {
        currentUserId = null;
    }

    public boolean isLoggedin() {
        return currentUserId != null;
    }

    public void login(String userId) {
        currentUserId = userId;
    }

    public void logout() {
        currentUserId = null;

    }
}
