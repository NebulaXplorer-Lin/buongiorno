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
        SocialNetwork network = new SocialNetwork();

        try (Scanner scanner = new Scanner(new File(filePath))) {
            Set<String> userLines = new HashSet<>();
            Set<String> friendLines = new HashSet<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith(USER_PREFIX)) {
                    userLines.add(line);
                } else if (line.startsWith(FRIEND_PREFIX)) {
                    friendLines.add(line);
                }
            }

            for (String line : userLines) {
                User user = parseUserLine(line);
                if (user != null) {
                    network.addUser(user);
                }
            }

            for (String line : friendLines) {
                parseFriendLine(line, network);
            }

        } catch (FileNotFoundException e) {
            return null;
        }

        return network;
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

    private User parseUserLine(String line) {
        if (!line.startsWith(USER_PREFIX)) {
            return null;
        }

        String[] parts = line.substring(USER_PREFIX.length() + 1).split(SEPARATOR);
        if (parts.length != 5) {
            return null;
        }

        String userId = parts[0].trim();
        String name = parts[1].trim();
        String workplace = parts[2].trim();
        String hometown = parts[3].trim();
        String passwordHash = parts[4].trim();

        return new User(userId, name, workplace, hometown, passwordHash);
    }

    private void parseFriendLine(String line, SocialNetwork network) {
        if (!line.startsWith(FRIEND_PREFIX)) {
            return;
        }

        String[] parts = line.substring(FRIEND_PREFIX.length() + 1).split(SEPARATOR);
        if (parts.length != 2) {
            return;
        }

        String userId1 = parts[0].trim();
        String userId2 = parts[1].trim();

        network.addFriendship(userId1, userId2);
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
}