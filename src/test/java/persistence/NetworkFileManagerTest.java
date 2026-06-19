package persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.SocialNetwork;
import model.User;

class NetworkFileManagerTest {
    @TempDir
    Path tempDirectory;

    @Test
    void removedFriendshipDoesNotReturnAfterSaveAndReload() {
        SocialNetwork network = new SocialNetwork();
        User alice = new User("alice", "Alice", "OpenAI", "Dundee", "hash");
        User bob = new User("bob", "Bob", "Google", "London", "hash");
        network.addUser(alice);
        network.addUser(bob);
        network.addFriendship("alice", "bob");
        network.removeFriendship("alice", "bob");

        Path saveFile = tempDirectory.resolve("network.txt");
        NetworkFileManager fileManager = new NetworkFileManager();
        fileManager.saveToFile(network, saveFile.toString());
        SocialNetwork loadedNetwork = fileManager.loadFromFile(saveFile.toString());

        assertNotNull(loadedNetwork);
        assertTrue(loadedNetwork.containsUser("alice"));
        assertTrue(loadedNetwork.containsUser("bob"));
        assertFalse(loadedNetwork.getUser("alice").hasFriend("bob"));
        assertFalse(loadedNetwork.getUser("bob").hasFriend("alice"));
    }
}
