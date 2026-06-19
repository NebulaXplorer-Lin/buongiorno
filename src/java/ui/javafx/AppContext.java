package ui.javafx;

import model.Session;
import model.SocialNetwork;
import model.User;
import persistence.NetworkFileManager;
import service.AuthService;
import service.FriendService;
import service.RecommendationService;
import service.UserService;

/**
 * Holds shared application objects for JavaFX controllers.
 */
public class AppContext {
    private final SocialNetwork network;
    private final Session session;
    private final AuthService authService;
    private final UserService userService;
    private final FriendService friendService;
    private final RecommendationService recommendationService;
    private final NetworkFileManager fileManager;

    /**
     * Creates a controller context.
     *
     * @param network shared social network
     * @param session shared session
     * @param authService authentication service
     * @param userService user service
     * @param friendService friendship service
     * @param recommendationService recommendation service
     * @param fileManager persistence service
     */
    public AppContext(
            SocialNetwork network,
            Session session,
            AuthService authService,
            UserService userService,
            FriendService friendService,
            RecommendationService recommendationService,
            NetworkFileManager fileManager) {
        this.network = network;
        this.session = session;
        this.authService = authService;
        this.userService = userService;
        this.friendService = friendService;
        this.recommendationService = recommendationService;
        this.fileManager = fileManager;
    }

    /**
     * Returns the shared social network.
     *
     * @return the shared social network
     */
    public SocialNetwork getNetwork() {
        return network;
    }

    /**
     * Returns the shared session.
     *
     * @return the shared session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns the authentication service.
     *
     * @return the authentication service
     */
    public AuthService getAuthService() {
        return authService;
    }

    /**
     * Returns the user service.
     *
     * @return the user service
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * Returns the friendship service.
     *
     * @return the friendship service
     */
    public FriendService getFriendService() {
        return friendService;
    }

    /**
     * Returns the recommendation service.
     *
     * @return the recommendation service
     */
    public RecommendationService getRecommendationService() {
        return recommendationService;
    }

    /**
     * Returns the network file manager.
     *
     * @return the network file manager
     */
    public NetworkFileManager getFileManager() {
        return fileManager;
    }

    /**
     * Replaces the contents of the shared network while retaining the same
     * network object used by all services.
     *
     * @param loadedNetwork network data to copy into the shared model
     */
    public void replaceCurrentNetworkData(SocialNetwork loadedNetwork) {
        network.clear();

        for (User user : loadedNetwork.getAllUsers()) {
            network.addUser(user);
        }

        for (User user : loadedNetwork.getAllUsers()) {
            for (String friendId : user.getFriendIds()) {
                network.addFriendship(user.getUserId(), friendId);
            }
        }
    }
}
