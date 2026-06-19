package persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
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

    @Test
    void malformedUserRecordFailsInsteadOfReturningPartialData() throws IOException {
        Path saveFile = writeData(
                "USER,alice,Alice,OpenAI,Dundee,hash",
                "USER,bob,Bob,Google");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(saveFile.toString()));

        assertTrue(exception.getMessage().contains("line 2"));
        assertTrue(exception.getMessage().contains("USER record"));
    }

    @Test
    void malformedFriendRecordFailsInsteadOfBeingIgnored() throws IOException {
        Path saveFile = writeData(
                "USER,alice,Alice,OpenAI,Dundee,hash",
                "FRIEND,alice");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(saveFile.toString()));

        assertTrue(exception.getMessage().contains("line 2"));
        assertTrue(exception.getMessage().contains("FRIEND record"));
    }

    @Test
    void unknownRecordTypeFailsInsteadOfBeingIgnored() throws IOException {
        Path saveFile = writeData(
                "USER,alice,Alice,OpenAI,Dundee,hash",
                "GROUP,alice,admins");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(saveFile.toString()));

        assertTrue(exception.getMessage().contains("line 2"));
        assertTrue(exception.getMessage().contains("unknown record type"));
    }

    @Test
    void friendshipWithUnknownUserFailsInsteadOfBeingIgnored() throws IOException {
        Path saveFile = writeData(
                "USER,alice,Alice,OpenAI,Dundee,hash",
                "FRIEND,alice,bob");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(saveFile.toString()));

        assertTrue(exception.getMessage().contains("line 2"));
        assertTrue(exception.getMessage().contains("unknown user"));
    }

    @Test
    void duplicateUserRecordsAreNotSilentlyRemovedByHashSet() throws IOException {
        Path saveFile = writeData(
                "USER,alice,Alice,OpenAI,Dundee,hash-a",
                "USER,alice,Alice,OpenAI,Dundee,hash-a");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(saveFile.toString()));

        assertTrue(exception.getMessage().contains("duplicate or invalid user ID"));
    }

    @Test
    void emptyWorkplaceFailsBecauseAllUserFieldsAreRequired() throws IOException {
        Path saveFile = writeData(
                "USER,alice,Alice,,Dundee,hash-a");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(saveFile.toString()));

        assertTrue(exception.getMessage().contains("line 1"));
        assertTrue(exception.getMessage().contains("workplace must not be empty"));
    }

    @Test
    void emptyHometownFailsBecauseAllUserFieldsAreRequired() throws IOException {
        Path saveFile = writeData(
                "USER,alice,Alice,OpenAI,,hash-a");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(saveFile.toString()));

        assertTrue(exception.getMessage().contains("line 1"));
        assertTrue(exception.getMessage().contains("hometown must not be empty"));
    }

    @Test
    void validRecordsStillAllowForwardReferences() throws IOException {
        Path saveFile = writeData(
                "FRIEND,alice,bob",
                "USER,alice,Alice,OpenAI,Dundee,hash-a",
                "USER,bob,Bob,Google,London,hash-b");

        SocialNetwork network = new NetworkFileManager().loadFromFile(saveFile.toString());

        assertNotNull(network);
        assertTrue(network.getUser("alice").hasFriend("bob"));
        assertTrue(network.getUser("bob").hasFriend("alice"));
    }

    private Path writeData(String... lines) throws IOException {
        Path saveFile = tempDirectory.resolve("network.txt");
        Files.write(saveFile, java.util.List.of(lines));
        return saveFile;
    }
}
