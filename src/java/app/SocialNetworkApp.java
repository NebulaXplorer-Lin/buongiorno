package app;

import model.Session;
import model.SocialNetwork;
import service.AuthService;

public class SocialNetworkApp {
    private final SocialNetwork network;
    private final Session session;
    private final AuthService authService;

    public SocialNetworkApp() {
        network = new SocialNetwork();
        session = new Session();

        authService = new AuthService(network, session);
    }

    public ui.javafx.AppContext createContext() {
        return new ui.javafx.AppContext(
                network,
                session,
                authService);
    }
}
