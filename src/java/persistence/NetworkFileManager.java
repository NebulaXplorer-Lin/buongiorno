package persistence;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.SocialNetwork;
import model.User;

/**
 * Loads and saves social-network data in the application's comma-separated
 * {@code USER} and {@code FRIEND} record format.
 *
 * <p>Import validation preserves source line numbers and reports all detected
 * errors together. Error messages identify the record type, invalid field,
 * duplicate declaration, or unresolved friendship endpoint.</p>
 */
public class NetworkFileManager {

    private static final String USER_PREFIX = "USER";
    private static final String FRIEND_PREFIX = "FRIEND";
    private static final String SEPARATOR = ",";
    private static final int USER_FIELD_COUNT = 6;
    private static final int FRIEND_FIELD_COUNT = 3;

    /**
     * Creates a network file manager.
     */
    public NetworkFileManager() {
    }

    /**
     * Loads and validates a social network from a UTF-8 text file.
     *
     * <p>Blank lines and a UTF-8 byte-order mark on the first line are ignored.
     * {@code FRIEND} records may appear before their corresponding
     * {@code USER} records because friendships are validated after every user
     * declaration has been read.</p>
     *
     * <p>The method validates record types, field counts, required fields,
     * duplicate user IDs, self-friendships, duplicate undirected friendships,
     * and references to unknown users. If validation fails, one exception
     * contains the complete ordered error list and no partially populated
     * network is returned.</p>
     *
     * @param filePath path to the input file
     * @return a fully validated social network
     * @throws IllegalArgumentException if the path is empty, invalid, missing,
     *         not a readable regular file, or one or more records are invalid
     */
    public SocialNetwork loadFromFile(String filePath) {
        List<String> lines = readLines(filePath);
        List<String> errors = new ArrayList<>();
        List<UserRecord> userRecords = new ArrayList<>();
        List<FriendRecord> friendRecords = new ArrayList<>();
        Map<String, Integer> firstUserLineById = new HashMap<>();
        boolean containsRecord = false;

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String line = removeByteOrderMark(lines.get(index), lineNumber).trim();
            if (line.isEmpty()) {
                continue;
            }

            containsRecord = true;
            String[] parts = line.split(SEPARATOR, -1);
            String recordType = parts[0].trim();

            if (USER_PREFIX.equals(recordType)) {
                UserRecord record = parseUserRecord(parts, lineNumber, errors);
                if (record == null) {
                    continue;
                }

                Integer firstLine = firstUserLineById.putIfAbsent(record.userId(), lineNumber);
                if (firstLine != null) {
                    errors.add(recordError(
                            lineNumber,
                            USER_PREFIX,
                            "duplicate user ID '" + record.userId()
                                    + "'; it was first declared at line " + firstLine));
                } else {
                    userRecords.add(record);
                }
            } else if (FRIEND_PREFIX.equals(recordType)) {
                FriendRecord record = parseFriendRecord(parts, lineNumber, errors);
                if (record != null) {
                    friendRecords.add(record);
                }
            } else if (recordType.isEmpty()) {
                errors.add(lineError(
                        lineNumber,
                        "record type is empty; expected USER or FRIEND"));
            } else {
                errors.add(lineError(
                        lineNumber,
                        "unknown record type '" + recordType + "'; expected USER or FRIEND"));
            }
        }

        if (!containsRecord) {
            errors.add("file contains no USER or FRIEND records");
        }

        validateFriendRecords(friendRecords, firstUserLineById, errors);

        if (!errors.isEmpty()) {
            throw importFailure(errors);
        }

        SocialNetwork network = new SocialNetwork();
        for (UserRecord record : userRecords) {
            network.addUser(new User(
                    record.userId(),
                    record.name(),
                    record.workplace(),
                    record.hometown(),
                    record.passwordHash()));
        }

        for (FriendRecord record : friendRecords) {
            network.addFriendship(record.userId1(), record.userId2());
        }

        return network;
    }

    /**
     * Saves all users and unique undirected friendships to a text file.
     *
     * @param network network to save
     * @param filePath destination file path
     * @throws RuntimeException if the file cannot be written
     */
    public void saveToFile(SocialNetwork network, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (User user : network.getAllUsers()) {
                writer.println(formatUserLine(user));
            }

            Set<String> writtenFriendships = new HashSet<>();
            for (User user : network.getAllUsers()) {
                String userId1 = user.getUserId();
                for (String userId2 : user.getFriendIds()) {
                    String friendshipKey = createFriendshipKey(userId1, userId2);
                    if (!writtenFriendships.contains(friendshipKey)) {
                        writer.println(formatFriendLine(userId1, userId2));
                        writtenFriendships.add(friendshipKey);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save network to file: " + filePath, e);
        }
    }

    private List<String> readLines(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw importFailure(List.of("file path must not be empty"));
        }

        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                throw importFailure(List.of("file does not exist: " + path.toAbsolutePath()));
            }
            if (!Files.isRegularFile(path)) {
                throw importFailure(List.of("path is not a regular file: " + path.toAbsolutePath()));
            }
            if (!Files.isReadable(path)) {
                throw importFailure(List.of("file is not readable: " + path.toAbsolutePath()));
            }
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (InvalidPathException exception) {
            throw importFailure(List.of("file path is invalid: " + exception.getInput()), exception);
        } catch (IOException | SecurityException exception) {
            String detail = exception.getMessage();
            String reason = "could not read file '" + filePath + "'";
            if (detail != null && !detail.isBlank()) {
                reason += ": " + detail;
            }
            throw importFailure(List.of(reason), exception);
        }
    }

    private UserRecord parseUserRecord(String[] parts, int lineNumber, List<String> errors) {
        if (parts.length != USER_FIELD_COUNT) {
            errors.add(recordError(
                    lineNumber,
                    USER_PREFIX,
                    "USER record must contain exactly 6 comma-separated fields "
                            + "(USER,userId,userName,workplace,hometown,passwordHash), but found "
                            + parts.length));
            return null;
        }

        int errorCountBeforeParsing = errors.size();
        String userId = requireNonEmpty(parts[1], lineNumber, USER_PREFIX, 2, "user ID", errors);
        String name = requireNonEmpty(parts[2], lineNumber, USER_PREFIX, 3, "user name", errors);
        String workplace = requireNonEmpty(parts[3], lineNumber, USER_PREFIX, 4, "workplace", errors);
        String hometown = requireNonEmpty(parts[4], lineNumber, USER_PREFIX, 5, "hometown", errors);
        String passwordHash = requireNonEmpty(parts[5], lineNumber, USER_PREFIX, 6, "password hash", errors);

        if (errors.size() > errorCountBeforeParsing) {
            return null;
        }
        return new UserRecord(userId, name, workplace, hometown, passwordHash, lineNumber);
    }

    private FriendRecord parseFriendRecord(String[] parts, int lineNumber, List<String> errors) {
        if (parts.length != FRIEND_FIELD_COUNT) {
            errors.add(recordError(
                    lineNumber,
                    FRIEND_PREFIX,
                    "FRIEND record must contain exactly 3 comma-separated fields "
                            + "(FRIEND,userId1,userId2), but found " + parts.length));
            return null;
        }

        int errorCountBeforeParsing = errors.size();
        String userId1 = requireNonEmpty(parts[1], lineNumber, FRIEND_PREFIX, 2, "first user ID", errors);
        String userId2 = requireNonEmpty(parts[2], lineNumber, FRIEND_PREFIX, 3, "second user ID", errors);
        if (errors.size() > errorCountBeforeParsing) {
            return null;
        }
        if (userId1.equals(userId2)) {
            errors.add(recordError(
                    lineNumber,
                    FRIEND_PREFIX,
                    "self-friendship is not allowed for user ID '" + userId1 + "'"));
            return null;
        }

        return new FriendRecord(userId1, userId2, lineNumber);
    }

    private String requireNonEmpty(
            String value,
            int lineNumber,
            String recordType,
            int fieldNumber,
            String fieldName,
            List<String> errors) {
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            errors.add(recordError(
                    lineNumber,
                    recordType,
                    "field " + fieldNumber + " (" + fieldName + ") must not be empty"));
            return null;
        }
        return trimmedValue;
    }

    private void validateFriendRecords(
            List<FriendRecord> friendRecords,
            Map<String, Integer> userLinesById,
            List<String> errors) {
        Map<String, Integer> firstFriendshipLineByKey = new HashMap<>();

        for (FriendRecord record : friendRecords) {
            List<String> unknownUserIds = new ArrayList<>();
            if (!userLinesById.containsKey(record.userId1())) {
                unknownUserIds.add("'" + record.userId1() + "'");
            }
            if (!userLinesById.containsKey(record.userId2())) {
                unknownUserIds.add("'" + record.userId2() + "'");
            }

            if (!unknownUserIds.isEmpty()) {
                errors.add(recordError(
                        record.lineNumber(),
                        FRIEND_PREFIX,
                        "unknown user ID" + (unknownUserIds.size() == 1 ? " " : "s ")
                                + String.join(" and ", unknownUserIds)
                                + "; every FRIEND endpoint must have a USER record"));
                continue;
            }

            String friendshipKey = createFriendshipKey(record.userId1(), record.userId2());
            Integer firstLine = firstFriendshipLineByKey.putIfAbsent(friendshipKey, record.lineNumber());
            if (firstLine != null) {
                errors.add(recordError(
                        record.lineNumber(),
                        FRIEND_PREFIX,
                        "duplicate friendship between '" + record.userId1() + "' and '"
                                + record.userId2() + "'; it was first declared at line " + firstLine));
            }
        }
    }

    private String removeByteOrderMark(String line, int lineNumber) {
        if (lineNumber == 1 && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }

    private String lineError(int lineNumber, String reason) {
        return "line " + lineNumber + ": " + reason;
    }

    private String recordError(int lineNumber, String recordType, String reason) {
        return "line " + lineNumber + " [" + recordType + "]: " + reason;
    }

    private IllegalArgumentException importFailure(List<String> errors) {
        return importFailure(errors, null);
    }

    private IllegalArgumentException importFailure(List<String> errors, Exception cause) {
        StringBuilder message = new StringBuilder("Network data import failed");
        if (errors.size() == 1) {
            message.append(" with 1 error:");
        } else {
            message.append(" with ").append(errors.size()).append(" errors:");
        }
        for (String error : errors) {
            message.append(System.lineSeparator()).append("- ").append(error);
        }
        return new IllegalArgumentException(message.toString(), cause);
    }

    private String formatUserLine(User user) {
        return String.join(SEPARATOR,
                USER_PREFIX,
                user.getUserId(),
                user.getUserName(),
                user.getWorkplace() != null ? user.getWorkplace() : "",
                user.getHometown() != null ? user.getHometown() : "",
                user.getPasswordHash());
    }

    private String formatFriendLine(String userId1, String userId2) {
        return String.join(SEPARATOR, FRIEND_PREFIX, userId1, userId2);
    }

    private String createFriendshipKey(String userId1, String userId2) {
        if (userId1.compareTo(userId2) <= 0) {
            return userId1 + ":" + userId2;
        } else {
            return userId2 + ":" + userId1;
        }
    }

    private record UserRecord(
            String userId,
            String name,
            String workplace,
            String hometown,
            String passwordHash,
            int lineNumber) {
    }

    private record FriendRecord(String userId1, String userId2, int lineNumber) {
    }
}
