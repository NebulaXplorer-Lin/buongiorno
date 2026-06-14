
package app;

import model.Session;
import model.SocialNetwork;
import service.AuthService;

public class AppContext {
    private final SocialNetwork network;
    private final Session session;
    private final AuthService authService;

    public AppContext(SocialNetwork network, Session session, AuthService authService) {
        this.network = network;
        this.session = session;
        this.authService = authService;
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
}
