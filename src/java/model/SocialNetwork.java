package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SocialNetwork {
    private Map<String, User> users;
    private Map<String, Set<String>> usersByWorkplace;
    private Map<String, Set<String>> usersByHometown;

    public SocialNetwork() {
        users = new HashMap<>();
        usersByWorkplace = new HashMap<>();
        usersByHometown = new HashMap<>();
    }

    public boolean addUser(User user) {
        if (user == null)
            return false;

        String userId = user.getUserId();

        if (userId == null)
            return false;

        if (users.containsKey(userId))
            return false;

        users.put(userId, user);
        indexUser(user);
        return true;

    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public boolean containsUser(String userId) {
        return users.containsKey(userId);
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public Set<User> getUsersByWorkplace(String workplace) {
        return getUsersFromIndex(usersByWorkplace, workplace);
    }

    public Set<User> getUsersByHometown(String hometown) {
        return getUsersFromIndex(usersByHometown, hometown);
    }

    public boolean updateUserProfile(String userId, String name, String workplace, String hometown) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }

        removeUserFromIndexes(user);
        user.setUserName(name);
        user.setWorkplace(workplace);
        user.setHometown(hometown);
        indexUser(user);

        return true;
    }

    public boolean addFriendship(String userId1, String userId2) {
        if (userId1 == null || userId2 == null)
            return false;
        if (!users.containsKey(userId1) || !users.containsKey(userId2))
            return false;

        User user1 = users.get(userId1);
        User user2 = users.get(userId2);

        if (user1 == null || user2 == null)
            return false;

        user1.addFriend(userId2);
        user2.addFriend(userId1);

        return true;

    }

    public boolean removeFriendship(String userId1, String userId2) {
        if (userId1 == null || userId2 == null)
            return false;
        if (!users.containsKey(userId1) || !users.containsKey(userId2))
            return false;

        User user1 = users.get(userId1);
        User user2 = users.get(userId2);

        if (user1 == null || user2 == null)
            return false;

        user1.removeFriend(userId2);
        user2.removeFriend(userId1);

        return true;
    }

    public void clear() {
        users.clear();
        usersByWorkplace.clear();
        usersByHometown.clear();
    }

    private void indexUser(User user) {
        addToIndex(usersByWorkplace, user.getWorkplace(), user.getUserId());
        addToIndex(usersByHometown, user.getHometown(), user.getUserId());
    }

    private void removeUserFromIndexes(User user) {
        removeFromIndex(usersByWorkplace, user.getWorkplace(), user.getUserId());
        removeFromIndex(usersByHometown, user.getHometown(), user.getUserId());
    }

    private void addToIndex(Map<String, Set<String>> index, String value, String userId) {
        String key = normalizeIndexKey(value);
        if (key == null) {
            return;
        }

        index.computeIfAbsent(key, ignored -> new HashSet<>()).add(userId);
    }

    private void removeFromIndex(Map<String, Set<String>> index, String value, String userId) {
        String key = normalizeIndexKey(value);
        if (key == null) {
            return;
        }

        Set<String> userIds = index.get(key);
        if (userIds == null) {
            return;
        }

        userIds.remove(userId);
        if (userIds.isEmpty()) {
            index.remove(key);
        }
    }

    private Set<User> getUsersFromIndex(Map<String, Set<String>> index, String value) {
        String key = normalizeIndexKey(value);
        if (key == null) {
            return new HashSet<>();
        }

        Set<String> userIds = index.get(key);
        if (userIds == null) {
            return new HashSet<>();
        }

        Set<User> indexedUsers = new HashSet<>();
        for (String userId : userIds) {
            User user = users.get(userId);
            if (user != null) {
                indexedUsers.add(user);
            }
        }

        return indexedUsers;
    }

    private String normalizeIndexKey(String value) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        if (normalizedValue.isEmpty()) {
            return null;
        }

        return normalizedValue;
    }

}
