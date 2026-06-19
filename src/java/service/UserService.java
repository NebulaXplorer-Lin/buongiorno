package service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import model.Session;
import model.SocialNetwork;
import model.User;

/**
 * Provides profile access and user search operations.
 */
public class UserService {

    private SocialNetwork network;
    private Session session;

    /**
     * Creates a user service.
     *
     * @param network social network to query and update
     * @param session source of the current user ID
     */
    public UserService(SocialNetwork network, Session session) {
        this.network = network;
        this.session = session;
    }

    /**
     * Returns the currently authenticated user.
     *
     * @return the current user, or {@code null} when logged out
     */
    public User getCurrentUser() {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null) {
            return null;
        }
        return network.getUser(currentUserId);
    }

    /**
     * Updates the current user's profile.
     *
     * @param name new display name
     * @param workplace new workplace
     * @param hometown new hometown
     * @return {@code true} when a logged-in user was updated
     */
    public boolean updateCurrentUserProfile(String name, String workplace, String hometown) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return network.updateUserProfile(currentUser.getUserId(), name, workplace, hometown);
    }

    /**
     * Returns all registered users.
     *
     * @return a read-only collection of users
     */
    public Collection<User> getAllUsers() {
        return network.getAllUsers();
    }

    /**
     * Looks up a user by ID.
     *
     * @param userId user identifier
     * @return matching user, or {@code null} when absent
     */
    public User getUserById(String userId) {
        return network.getUser(userId);
    }

    /**
     * Searches by user ID, name, workplace, or hometown. Search-type labels are
     * case-insensitive; names are matched case-insensitively, while attribute
     * searches use the network indexes.
     *
     * @param searchType one of {@code User ID}, {@code Name},
     *        {@code Workplace}, or {@code Hometown}
     * @param searchValue value to match
     * @return matching users, or an empty set for invalid input
     */
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

    /**
     * Finds users by hometown.
     *
     * @param hometown hometown to search for
     * @return matching users
     */
    public Set<User> findUsersByHometown(String hometown) {
        return network.getUsersByHometown(hometown);
    }

    /**
     * Finds users by workplace.
     *
     * @param workplace workplace to search for
     * @return matching users
     */
    public Set<User> findUsersByWorkplace(String workplace) {
        return network.getUsersByWorkplace(workplace);
    }
}
