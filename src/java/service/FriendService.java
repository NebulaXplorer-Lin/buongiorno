package service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Session;
import model.SocialNetwork;
import model.User;

public class FriendService {

    private SocialNetwork network;
    private Session session;

    public FriendService(SocialNetwork network, Session session) {
        this.network = network;
        this.session = session;
    }

    public List<User> getCurrentUserFriends() {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null) {
            return new ArrayList<>();
        }
        return getFriendsOfUser(currentUserId);
    }

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

    public boolean areFriends(String userId1, String userId2) {
        User user1 = network.getUser(userId1);
        User user2 = network.getUser(userId2);

        if (user1 == null || user2 == null) {
            return false;
        }

        return user1.getFriendIds().contains(userId2);
    }
}
