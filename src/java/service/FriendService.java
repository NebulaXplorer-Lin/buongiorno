package service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Session;
import model.SocialNetwork;
import model.User;

/**
 * Provides friendship queries and operations for the current session.
 */
public class FriendService {

    private SocialNetwork network;
    private Session session;

    /**
     * Creates a friendship service.
     *
     * @param network social network to query and modify
     * @param session source of the current user ID
     */
    public FriendService(SocialNetwork network, Session session) {
        this.network = network;
        this.session = session;
    }

    /**
     * Returns the current user's friends.
     *
     * @return current friends, or an empty list when no user is logged in
     */
    public List<User> getCurrentUserFriends() {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null) {
            return new ArrayList<>();
        }
        return getFriendsOfUser(currentUserId);
    }

    /**
     * Returns a user's friends, excluding the currently logged-in user.
     *
     * @param userId user whose friends should be returned
     * @return matching users, or an empty list when the user does not exist
     */
    public List<User> getFriendsOfUser(String userId) {
        String currentUserId = session.getCurrentUserId();
        User user = network.getUser(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        List<User> friends = new ArrayList<>();
        for (String friendId : user.getFriendIds()) {
            if (friendId.equals(currentUserId)) {
                continue;
            }

            User friend = network.getUser(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return friends;
    }

    /**
     * Adds a friendship between the current user and another user.
     *
     * @param friendId user ID to add
     * @return {@code true} when a new friendship is created
     */
    public boolean addFriendToCurrentUser(String friendId) {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        if (!network.containsUser(friendId)) {
            return false;
        }

        if (currentUserId.equals(friendId)) {
            return false;
        }

        if (areFriends(currentUserId, friendId)) {
            return false;
        }

        return network.addFriendship(currentUserId, friendId);
    }

    /**
     * Removes a friendship between the current user and another user.
     *
     * @param friendId user ID to remove
     * @return {@code true} when an existing friendship is removed
     */
    public boolean removeFriendFromCurrentUser(String friendId) {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null || friendId == null) {
            return false;
        }

        if (!network.containsUser(friendId)) {
            return false;
        }

        if (currentUserId.equals(friendId)) {
            return false;
        }

        if (!areFriends(currentUserId, friendId)) {
            return false;
        }

        return network.removeFriendship(currentUserId, friendId);
    }

    /**
     * Finds users who are friends with both the current user and another user.
     *
     * @param otherUserId other user's ID
     * @return common friends, or an empty set when either user is unavailable
     */
    public Set<User> getCommonFriends(String otherUserId) {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null) {
            return new HashSet<>();
        }

        User currentUser = network.getUser(currentUserId);
        User otherUser = network.getUser(otherUserId);

        if (currentUser == null || otherUser == null) {
            return new HashSet<>();
        }

        Set<String> currentFriendIds = currentUser.getFriendIds();
        Set<String> otherFriendIds = otherUser.getFriendIds();

        Set<String> commonIds;
        if (currentFriendIds.size() <= otherFriendIds.size()) {
            commonIds = new HashSet<>(currentFriendIds);
            commonIds.retainAll(otherFriendIds);
        } else {
            commonIds = new HashSet<>(otherFriendIds);
            commonIds.retainAll(currentFriendIds);
        }

        Set<User> commonFriends = new HashSet<>();
        for (String id : commonIds) {
            User user = network.getUser(id);
            if (user != null) {
                commonFriends.add(user);
            }
        }

        return commonFriends;
    }

    /**
     * Filters the current user's friends by exact hometown.
     *
     * @param hometown hometown to match
     * @return friends with the specified hometown
     */
    public List<User> filterCurrentUserFriendsByHometown(String hometown) {
        List<User> allFriends = getCurrentUserFriends();
        List<User> filtered = new ArrayList<>();
        for (User friend : allFriends) {
            String friendHometown = friend.getHometown();
            if (friendHometown != null && friendHometown.equals(hometown)) {
                filtered.add(friend);
            }
        }
        return filtered;
    }

    /**
     * Filters the current user's friends by exact workplace.
     *
     * @param workplace workplace to match
     * @return friends with the specified workplace
     */
    public List<User> filterCurrentUserFriendsByWorkplace(String workplace) {
        List<User> allFriends = getCurrentUserFriends();
        List<User> filtered = new ArrayList<>();
        for (User friend : allFriends) {
            String friendWorkplace = friend.getWorkplace();
            if (friendWorkplace != null && friendWorkplace.equals(workplace)) {
                filtered.add(friend);
            }
        }
        return filtered;
    }

    /**
     * Tests whether two existing users are friends.
     *
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return {@code true} when the first user's friend set contains the second
     *         user
     */
    public boolean areFriends(String userId1, String userId2) {
        User user1 = network.getUser(userId1);
        User user2 = network.getUser(userId2);

        if (user1 == null || user2 == null) {
            return false;
        }

        return user1.getFriendIds().contains(userId2);
    }
}
