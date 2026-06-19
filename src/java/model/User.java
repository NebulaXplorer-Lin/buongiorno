package model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a social-network user and the identifiers of that user's
 * friends.
 */
public class User {
    private String userId;
    private String userName;
    private String workplace;
    private String hometown;
    private String passwordHash;
    private Set<String> friendIds;

    /**
     * Creates a user.
     *
     * @param userIdString unique user identifier
     * @param userNameString display name
     * @param workplaceString workplace
     * @param hometownString hometown
     * @param passwordHashString encoded password hash
     */
    public User(String userIdString, String userNameString, String workplaceString, String hometownString,
            String passwordHashString) {
        this.userId = userIdString;
        this.userName = userNameString;
        this.workplace = workplaceString;
        this.hometown = hometownString;
        this.passwordHash = passwordHashString;
        this.friendIds = new HashSet<>();
    }

    /**
     * Returns the unique user identifier.
     *
     * @return the unique user identifier
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the user's display name.
     *
     * @return the user's display name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the user's workplace.
     *
     * @return the user's workplace
     */
    public String getWorkplace() {
        return workplace;
    }

    /**
     * Returns the user's hometown.
     *
     * @return the user's hometown
     */
    public String getHometown() {
        return hometown;
    }

    /**
     * Returns the encoded password hash.
     *
     * @return the encoded password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Returns the mutable set of friend identifiers maintained by the network
     * model.
     *
     * @return friend user IDs
     */
    public Set<String> getFriendIds() {
        return friendIds;
    }

    /**
     * Replaces the encoded password hash.
     *
     * @param passwordHash new encoded password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Updates the workplace.
     *
     * @param workplace new workplace
     */
    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    /**
     * Updates the hometown.
     *
     * @param hometown new hometown
     */
    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    /**
     * Updates the display name.
     *
     * @param userName new display name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Adds a friend identifier.
     *
     * @param friendId friend user ID
     */
    public void addFriend(String friendId) {
        friendIds.add(friendId);
    }

    /**
     * Removes a friend identifier.
     *
     * @param friendId friend user ID
     */
    public void removeFriend(String friendId) {
        friendIds.remove(friendId);
    }

    /**
     * Tests whether the specified user is a friend.
     *
     * @param friendId user ID to test
     * @return {@code true} when the ID is in this user's friend set
     */
    public boolean hasFriend(String friendId) {
        return friendIds.contains(friendId);
    }

}
