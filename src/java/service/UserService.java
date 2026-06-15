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

        return network.updateUserProfile(currentUser.getUserId(), name, workplace, hometown);
    }

    public Collection<User> getAllUsers() {
        return network.getAllUsers();
    }

    public User getUserById(String userId) {
        return network.getUser(userId);
    }

    public Set<User> searchUsers(String searchType, String searchValue) {
        Set<User> users = new HashSet<>();
        if (searchType == null || searchValue == null || searchValue.trim().isEmpty()) {
            return users;
        }

        String normalizedSearchType = searchType.trim().toLowerCase();
        String normalizedSearchValue = searchValue.trim();

        switch (normalizedSearchType) {
            case "user id":
                User user = network.getUser(normalizedSearchValue);
                if (user != null) {
                    users.add(user);
                }
                break;
            case "name":
                for (User currentUser : network.getAllUsers()) {
                    String currentUserName = currentUser.getUserName();
                    if (currentUserName != null && currentUserName.equalsIgnoreCase(normalizedSearchValue)) {
                        users.add(currentUser);
                    }
                }
                break;
            case "workplace":
                users.addAll(network.getUsersByWorkplace(normalizedSearchValue));
                break;
            case "hometown":
                users.addAll(network.getUsersByHometown(normalizedSearchValue));
                break;
            default:
                break;
        }

        return users;
    }

    public Set<User> findUsersByHometown(String hometown) {
        return network.getUsersByHometown(hometown);
    }

    public Set<User> findUsersByWorkplace(String workplace) {
        return network.getUsersByWorkplace(workplace);
    }
}
