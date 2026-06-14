package service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import model.Session;
import model.SocialNetwork;
import model.User;

public class UserService {

    private SocialNetwork network;
    private Session session;

    public UserService(SocialNetwork network, Session session) {
        this.network = network;
        this.session = session;
    }

    public User getCurrentUser() {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null) {
            return null;
        }
        return network.getUser(currentUserId);
    }

    public boolean updateCurrentUserProfile(String name, String workplace, String hometown) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        currentUser.setUserName(name);
        currentUser.setWorkplace(workplace);
        currentUser.setHometown(hometown);

        return true;
    }

    public Collection<User> getAllUsers() {
        return network.getAllUsers();
    }

    public User getUserById(String userId) {
        return network.getUser(userId);
    }

    public Set<User> findUsersByHometown(String hometown) {
        Set<User> users = new HashSet<>();
        for (User user : network.getAllUsers()) {
            String userHometown = user.getHometown();
            if (userHometown != null && userHometown.equals(hometown)) {
                users.add(user);
            }
        }
        return users;
    }

    public Set<User> findUsersByWorkplace(String workplace) {
        Set<User> users = new HashSet<>();
        for (User user : network.getAllUsers()) {
            String userWorkplace = user.getWorkplace();
            if (userWorkplace != null && userWorkplace.equals(workplace)) {
                users.add(user);
            }
        }
        return users;
    }
}