package persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.SocialNetwork;

class NetworkFileValidationTest {
    @TempDir
    Path tempDirectory;

    @Test
    void reportsUserAndFriendErrorsTogetherWithTheirRecordTypes() throws IOException {
        Path file = writeData(
                "USER,u001,Alice,OpenAI,Dundee,hash-a",
                "USER,u001,Alice Again,OpenAI,Dundee,hash-b",
                "FRIEND,,",
                "FRIEND,u001,missing");

        IllegalArgumentException exception = assertImportFails(file);
        String message = exception.getMessage();

        assertTrue(message.contains("with 4 errors"));
        assertTrue(message.contains("line 2 [USER]: duplicate user ID 'u001'"));
        assertTrue(message.contains("line 3 [FRIEND]: field 2 (first user ID) must not be empty"));
        assertTrue(message.contains("line 3 [FRIEND]: field 3 (second user ID) must not be empty"));
        assertTrue(message.contains("line 4 [FRIEND]: unknown user ID 'missing'"));
    }

    @Test
    void reportsExactFieldCountsAndExpectedFormats() throws IOException {
        Path file = writeData(
                "USER,u001,Alice",
                "FRIEND,u001,u002,extra");

        String message = assertImportFails(file).getMessage();

        assertTrue(message.contains("line 1 [USER]"));
        assertTrue(message.contains("USER record must contain exactly 6"));
        assertTrue(message.contains("(USER,userId,userName,workplace,hometown,passwordHash)"));
        assertTrue(message.contains("line 2 [FRIEND]"));
        assertTrue(message.contains("FRIEND record must contain exactly 3"));
    }

    @Test
    void reportsUnknownRecordTypesAndMissingRecordTypes() throws IOException {
        Path file = writeData(
                "GROUP,u001,admins",
                ",u001,Alice");

        String message = assertImportFails(file).getMessage();

        assertTrue(message.contains("line 1: unknown record type 'GROUP'"));
        assertTrue(message.contains("line 2: record type is empty"));
        assertTrue(message.contains("expected USER or FRIEND"));
    }

    @Test
    void reportsSelfFriendshipsAndReversedDuplicateFriendships() throws IOException {
        Path file = writeData(
                "USER,u001,Alice,OpenAI,Dundee,hash-a",
                "USER,u002,Bob,Google,London,hash-b",
                "FRIEND,u001,u001",
                "FRIEND,u001,u002",
                "FRIEND,u002,u001");

        String message = assertImportFails(file).getMessage();

        assertTrue(message.contains("line 3 [FRIEND]: self-friendship is not allowed"));
        assertTrue(message.contains("line 5 [FRIEND]: duplicate friendship"));
        assertTrue(message.contains("first declared at line 4"));
    }

    @Test
    void reportsBothUnknownFriendEndpoints() throws IOException {
        Path file = writeData(
                "USER,u001,Alice,OpenAI,Dundee,hash-a",
                "FRIEND,missing-a,missing-b");

        String message = assertImportFails(file).getMessage();

        assertTrue(message.contains("line 2 [FRIEND]: unknown user IDs 'missing-a' and 'missing-b'"));
    }

    @Test
    void acceptsForwardReferencesBlankLinesAndUtf8ByteOrderMark() throws IOException {
        Path file = tempDirectory.resolve("network.txt");
        Files.writeString(
                file,
                "\uFEFFFRIEND,u001,u002\n\n"
                        + "USER,u001,Alice,OpenAI,Dundee,hash-a\n"
                        + "USER,u002,Bob,Google,London,hash-b\n",
                StandardCharsets.UTF_8);

        SocialNetwork network = new NetworkFileManager().loadFromFile(file.toString());

        assertEquals(2, network.getAllUsers().size());
        assertTrue(network.getUser("u001").hasFriend("u002"));
        assertTrue(network.getUser("u002").hasFriend("u001"));
    }

    @Test
    void rejectsEmptyFilesAndMissingFilesWithClearMessages() throws IOException {
        Path emptyFile = writeData("", "   ");
        String emptyMessage = assertImportFails(emptyFile).getMessage();
        assertTrue(emptyMessage.contains("file contains no USER or FRIEND records"));

        Path missingFile = tempDirectory.resolve("missing.txt");
        String missingMessage = assertImportFails(missingFile).getMessage();
        assertTrue(missingMessage.contains("file does not exist"));
        assertTrue(missingMessage.contains("missing.txt"));
    }

    @Test
    void rejectsEmptyFilePaths() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile("  "));

        assertTrue(exception.getMessage().contains("file path must not be empty"));
    }

    private IllegalArgumentException assertImportFails(Path file) {
        return assertThrows(
                IllegalArgumentException.class,
                () -> new NetworkFileManager().loadFromFile(file.toString()));
    }

    private Path writeData(String... lines) throws IOException {
        Path file = tempDirectory.resolve("network.txt");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return file;
    }
}
