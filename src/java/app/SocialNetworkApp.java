package app;

import model.Session;
import model.SocialNetwork;
import persistence.NetworkFileManager;
import service.AuthService;
import service.FriendService;
import service.RecommendationService;
import service.UserService;

/**
 * Creates and connects the model, persistence, and service-layer objects used
 * by the application.
 */
public class SocialNetworkApp {
    private final SocialNetwork network;
    private final Session session;
    private final AuthService authService;
    private final UserService userService;
    private final FriendService friendService;
    private final RecommendationService recommendationService;
    private final NetworkFileManager fileManager;

    /**
     * Creates an application composition root backed by an empty social network
     * and a new session.
     */
    public SocialNetworkApp() {
        network = new SocialNetwork();
        session = new Session();

        authService = new AuthService(network, session);
        userService = new UserService(network, session);
        friendService = new FriendService(network, session);
        recommendationService = new RecommendationService(network, session);
        fileManager = new NetworkFileManager();
    }

    /**
     * Creates a JavaFX context containing the application's shared objects.
     *
     * @return a context for use by JavaFX controllers
     */
    public ui.javafx.AppContext createContext() {
        return new ui.javafx.AppContext(
                network,
                session,
                authService,
                userService,
                friendService,
                recommendationService,
                fileManager);
    }
}
