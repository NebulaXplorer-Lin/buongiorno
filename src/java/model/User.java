package model;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String userId;
    private String userName;
    private String workplace;
    private String hometown;
    private String passwordHash;
    private Set<String> friendIds;

    public User(String userIdString, String userNameString, String workplaceString, String hometownString,
            String passwordHashString) {
        this.userId = userIdString;
        this.userName = userNameString;
        this.workplace = workplaceString;
        this.hometown = hometownString;
        this.passwordHash = passwordHashString;
        this.friendIds = new HashSet<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getWorkplace() {
        return workplace;
    }

    public String getHometown() {
        return hometown;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<String> getFriendIds() {
        return friendIds;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void addFriend(String friendId) {
        friendIds.add(friendId);
    }

    public void removeFriend(String friendId) {
        friendIds.remove(friendId);
    }

    public boolean hasFriend(String friendId) {
        return friendIds.contains(friendId);
    }

}
