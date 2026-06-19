package service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Session;
import model.SocialNetwork;
import model.User;

class FriendServiceTest {
    private SocialNetwork network;
    private Session session;
    private FriendService friendService;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        network = new SocialNetwork();
        session = new Session();
        friendService = new FriendService(network, session);
        alice = new User("alice", "Alice", "OpenAI", "Dundee", "hash");
        bob = new User("bob", "Bob", "Google", "London", "hash");
        network.addUser(alice);
        network.addUser(bob);
    }

    @Test
    void removingExistingFriendshipUpdatesBothUsers() {
        session.login("alice");
        network.addFriendship("alice", "bob");

        assertTrue(friendService.removeFriendFromCurrentUser("bob"));
        assertFalse(alice.hasFriend("bob"));
        assertFalse(bob.hasFriend("alice"));
    }

    @Test
    void removalFailsWhenNoUserIsLoggedIn() {
        network.addFriendship("alice", "bob");

        assertFalse(friendService.removeFriendFromCurrentUser("bob"));
        assertTrue(alice.hasFriend("bob"));
        assertTrue(bob.hasFriend("alice"));
    }

    @Test
    void removalRejectsNullUnknownSelfAndNonFriendTargets() {
        session.login("alice");

        assertFalse(friendService.removeFriendFromCurrentUser(null));
        assertFalse(friendService.removeFriendFromCurrentUser("missing"));
        assertFalse(friendService.removeFriendFromCurrentUser("alice"));
        assertFalse(friendService.removeFriendFromCurrentUser("bob"));
    }
}
