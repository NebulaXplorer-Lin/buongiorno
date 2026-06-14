
package app;

import model.Session;
import model.SocialNetwork;
import persistence.NetworkFileManager;
import service.AuthService;
import service.FriendService;
import service.RecommendationService;
import service.UserService;

public class AppContext {
    private final SocialNetwork network;
    private final Session session;
    private final AuthService authService;
    private final UserService userService;
    private final FriendService friendService;
    private final RecommendationService recommendationService;
    private final NetworkFileManager fileManager;

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

    public SocialNetwork getNetwork() {
        return network;
    }

    public Session getSession() {
        return session;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }

    public FriendService getFriendService() {
        return friendService;
    }

    public RecommendationService getRecommendationService() {
        return recommendationService;
    }

    public NetworkFileManager getFileManager() {
        return fileManager;
    }
}