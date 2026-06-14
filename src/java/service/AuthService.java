package service;

import model.Session;
import model.SocialNetwork;
import model.User;
import util.PasswordUtil;

public class AuthService {

    private SocialNetwork network;
    private Session session;

    public AuthService(SocialNetwork network, Session session) {
        this.network = network;
        this.session = session;
    }

    public boolean register(String userId, String password, String name, String workplace, String hometown) {
        if (isUserIdTaken(userId)) {
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User newUser = new User(userId, name, workplace, hometown, passwordHash);
        network.addUser(newUser);
        session.login(userId);

        return true;
    }

    public boolean login(String userId, String password) {
        User user = network.getUser(userId);
        if (user == null) {
            return false;
        }

        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            session.login(userId);
            return true;
        }

        return false;
    }

    public void logout() {
        session.logout();
    }

    public boolean isUserIdTaken(String userId) {
        return network.containsUser(userId);
    }
}
