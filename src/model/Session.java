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

/**
 * ### 5.3 `Session.java`
 * 
 * #### Responsibility
 * 
 * `Session` stores the current login state.
 * 
 * It tells the system whether a user is logged in and which user is currently
 * active.
 * 
 * #### Class
 * 
 * ```java
 * public class Session
 * ```
 * 
 * #### Fields
 * 
 * ```java
 * private String currentUserId;
 * ```
 * 
 * #### Field Explanation
 * 
 * | Field | Type | Purpose |
 * |---|---|---|
 * | `currentUserId` | `String` | Stores the ID of the logged-in user; `null`
 * means no user is logged in |
 * 
 * #### Key Methods
 * 
 * ```java
 * public boolean isLoggedIn()
 * public String getCurrentUserId()
 * public void login(String userId)
 * public void logout()
 * ```
 * 
 * #### Example State
 * 
 * Before login:
 * 
 * ```text
 * currentUserId = null
 * ```
 * 
 * After Alice logs in:
 * 
 * ```text
 * currentUserId = "u001"
 * ```
 * 
 * #### Design Reason
 * 
 * Using a `Session` means the user does not need to enter their own user ID for
 * every operation. All profile, friend, and recommendation actions can be
 * performed relative to
 * 
 * ## 6. `service` Folder
 * 
 * The `service` folder contains business logic. These classes use the model
 * classes but keep complex operations out of the UI.
 * 
 * ---
 */