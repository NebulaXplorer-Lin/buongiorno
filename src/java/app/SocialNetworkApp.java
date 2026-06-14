package app;

import model.Session;
import model.SocialNetwork;
import persistence.NetworkFileManager;
import service.AuthService;
import service.FriendService;
import service.RecommendationService;
import service.UserService;
import ui.ConsoleMenu;

public class SocialNetworkApp {
    private SocialNetwork network;
    private Session session;
    private AuthService authService;
    private UserService userService;
    private FriendService friendService;
    private RecommendationService recommendationService;
    private NetworkFileManager fileManager;
    private ConsoleMenu consoleMenu;

    public SocialNetworkApp() {
        network = new SocialNetwork();
        session = new Session();
        authService = new AuthService(network, session);
        userService = new UserService(network);
        friendService = new FriendService(network);
        recommendationService = new RecommendationService(network);
        fileManager = new NetworkFileManager(network);
        consoleMenu = new ConsoleMenu(authService, userService, friendService, recommendationService, fileManager);
    }

    public void run() {
        consoleMenu.start();
    }
}
