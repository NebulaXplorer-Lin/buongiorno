package persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import model.SocialNetwork;
import model.User;

public class NetworkFileManager {

    private static final String USER_PREFIX = "USER";
    private static final String FRIEND_PREFIX = "FRIEND";
    private static final String SEPARATOR = ",";

    public NetworkFileManager() {
    }

    public SocialNetwork loadFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            Set<UserRecord> userRecords = new HashSet<>();
            Set<FriendRecord> friendRecords = new HashSet<>();
            int lineNumber = 0;

            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(SEPARATOR, -1);
                String recordType = parts[0].trim();
                if (USER_PREFIX.equals(recordType)) {
                    userRecords.add(parseUserRecord(parts, lineNumber));
                } else if (FRIEND_PREFIX.equals(recordType)) {
                    friendRecords.add(parseFriendRecord(parts, lineNumber));
                } else {
                    throw malformedRecord(lineNumber, "unknown record type '" + recordType + "'");
                }
            }

            SocialNetwork network = new SocialNetwork();
            for (UserRecord record : userRecords) {
                User user = new User(
                        record.userId(),
                        record.name(),
                        record.workplace(),
                        record.hometown(),
                        record.passwordHash());
                if (!network.addUser(user)) {
                    throw malformedRecord(record.lineNumber(),
                            "duplicate or invalid user ID '" + record.userId() + "'");
                }
            }

            for (FriendRecord record : friendRecords) {
                if (!network.addFriendship(record.userId1(), record.userId2())) {
                    throw malformedRecord(record.lineNumber(),
                            "friendship references an unknown user");
                }
            }

            return network;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

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

    private UserRecord parseUserRecord(String[] parts, int lineNumber) {
        if (parts.length != 6) {
            throw malformedRecord(lineNumber, "USER record must contain 6 fields");
        }

        String userId = requireNonEmpty(parts[1], lineNumber, "user ID");
        String name = requireNonEmpty(parts[2], lineNumber, "user name");
        String workplace = requireNonEmpty(parts[3], lineNumber, "workplace");
        String hometown = requireNonEmpty(parts[4], lineNumber, "hometown");
        String passwordHash = requireNonEmpty(parts[5], lineNumber, "password hash");

        return new UserRecord(userId, name, workplace, hometown, passwordHash, lineNumber);
    }

    private FriendRecord parseFriendRecord(String[] parts, int lineNumber) {
        if (parts.length != 3) {
            throw malformedRecord(lineNumber, "FRIEND record must contain 3 fields");
        }

        String userId1 = requireNonEmpty(parts[1], lineNumber, "first user ID");
        String userId2 = requireNonEmpty(parts[2], lineNumber, "second user ID");
        if (userId1.equals(userId2)) {
            throw malformedRecord(lineNumber, "a user cannot be friends with itself");
        }

        return new FriendRecord(userId1, userId2, lineNumber);
    }

    private String requireNonEmpty(String value, int lineNumber, String fieldName) {
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw malformedRecord(lineNumber, fieldName + " must not be empty");
        }
        return trimmedValue;
    }

    private IllegalArgumentException malformedRecord(int lineNumber, String reason) {
        return new IllegalArgumentException("Malformed network data at line " + lineNumber + ": " + reason);
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
