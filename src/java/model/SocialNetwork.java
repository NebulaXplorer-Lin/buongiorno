package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SocialNetwork {
    private Map<String, User> users;

    public SocialNetwork() {
        users = new HashMap<>();
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
    }

}
