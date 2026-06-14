package app;

import model.Session;
import model.SocialNetwork;
import service.AuthService;
import ui.javafx.AppContext;

public class SocialNetworkApp {
    private final SocialNetwork network;
    private final Session session;
    private final AuthService authService;

    public SocialNetworkApp() {
        network = new SocialNetwork();
        session = new Session();

        authService = new AuthService(network, session);
    }

    public AppContext createContext() {
        return new AppContext(
                network,
                session,
                authService);
    }
}
