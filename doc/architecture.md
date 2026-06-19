# Social Network Console Application Architecture

## 1. Project Overview

This project is a Java console-based social networking application designed for the DI12010 group project. The system allows users to register, log in, manage their profile, add and view friends, inspect friends-of-friends, find common friends, filter friends, receive friend recommendations, and save/load the whole network from disk.

The application is designed around the idea that a social network is a graph:

- Each user is a node.
- Each friendship is an undirected edge.
- Each user's friend list is stored as a set of connected user IDs.

The selected implementation uses an adjacency-list graph structure:

```java
Map<String, User> users;
```

Inside each `User`:

```java
Set<String> friendIds;
```

This design is suitable for the assignment because it supports efficient user lookup, friendship checking, common-friend queries, friend recommendations, and file persistence.

---

## 2. Recommended Folder Structure

```text
src/
 |-- Main.java
 |-- app/
 |   `-- SocialNetworkApp.java
 |-- model/
 |   |-- User.java
 |   |-- SocialNetwork.java
 |   `-- Session.java
 |-- service/
 |   |-- AuthService.java
 |   |-- UserService.java
 |   |-- FriendService.java
 |   `-- RecommendationService.java
 |-- persistence/
 |   `-- NetworkFileManager.java
 |-- ui/
 |   `-- ConsoleMenu.java
 `-- util/
     `-- PasswordUtil.java
```

This structure separates the system into clear layers:

| Layer | Folder | Main Responsibility |
|---|---|---|
| Entry point | root | Starts the application |
| Application control | `app` | Connects all objects and starts the main loop |
| Data models | `model` | Stores application data |
| Business logic | `service` | Handles registration, login, users, friends, and recommendations |
| Persistence | `persistence` | Reads and writes the network to disk |
| User interface | `ui` | Handles console input/output |
| Utility | `util` | Provides helper logic such as password hashing |

---

## 3. Root File

### 3.1 `Main.java`

#### Responsibility

`Main.java` is the entry point of the whole program. It should not contain business logic. Its only job is to create the application object and start it.

#### Class

```java
public class Main
```

#### Fields

No fields are required.

#### Main Method

```java
public static void main(String[] args)
```

#### Example Role

```java
public class Main {
    public static void main(String[] args) {
        SocialNetworkApp app = new SocialNetworkApp();
        app.run();
    }
}
```

#### Design Reason

Keeping `Main` small makes the program easier to test, maintain, and explain in the report. It also shows that the system has a proper architecture rather than placing all logic in one file.

---

## 4. `app` Folder

The `app` folder contains the high-level application coordinator.

### 4.1 `SocialNetworkApp.java`

#### Responsibility

`SocialNetworkApp` builds and connects the major parts of the system:

- The social network data model.
- The current login session.
- Services for authentication, users, friendships, and recommendations.
- File persistence manager.
- Console menu.

It then starts the application loop.

#### Class

```java
public class SocialNetworkApp
```

#### Fields

```java
private SocialNetwork network;
private Session session;
private AuthService authService;
private UserService userService;
private FriendService friendService;
private RecommendationService recommendationService;
private NetworkFileManager fileManager;
private ConsoleMenu consoleMenu;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `network` | `SocialNetwork` | Stores all users and friendship relationships |
| `session` | `Session` | Stores the currently logged-in user ID |
| `authService` | `AuthService` | Handles register, login, and logout |
| `userService` | `UserService` | Handles profile viewing and editing |
| `friendService` | `FriendService` | Handles friend-related features |
| `recommendationService` | `RecommendationService` | Generates friend recommendations |
| `fileManager` | `NetworkFileManager` | Loads and saves the network |
| `consoleMenu` | `ConsoleMenu` | Runs the console UI |

#### Key Methods

```java
public SocialNetworkApp()
public void run()
```

#### Data Flow

```text
Main
 ↓
SocialNetworkApp
 ↓
Creates all model/service/UI objects
 ↓
ConsoleMenu.start()
```

#### Design Reason

This class prevents `Main` and `ConsoleMenu` from becoming overloaded. It acts as the composition root of the application.

---

## 5. `model` Folder

The `model` folder contains classes that represent the data structure of the application. These classes should mainly store data and provide basic operations.

---

### 5.1 `User.java`

#### Responsibility

`User` represents one social media user profile.

A user contains:

- Identity information.
- Profile information.
- Password hash for login.
- Friend connections.

#### Class

```java
public class User
```

#### Fields

```java
private String userId;
private String name;
private String workplace;
private String hometown;
private String passwordHash;
private Set<String> friendIds;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `userId` | `String` | Unique ID for the user |
| `name` | `String` | Display name |
| `workplace` | `String` | User's current workplace |
| `hometown` | `String` | User's hometown |
| `passwordHash` | `String` | Secure stored hash of the password |
| `friendIds` | `Set<String>` | IDs of this user's friends |

#### Key Methods

```java
public User(String userId, String name, String workplace, String hometown, String passwordHash)

public String getUserId()
public String getName()
public String getWorkplace()
public String getHometown()
public String getPasswordHash()
public Set<String> getFriendIds()

public void setName(String name)
public void setWorkplace(String workplace)
public void setHometown(String hometown)
public void setPasswordHash(String passwordHash)

public boolean addFriend(String friendId)
public boolean removeFriend(String friendId)
public boolean hasFriend(String friendId)
```

#### Important Design Point

`friendIds` should be a `HashSet<String>`, not an `ArrayList<String>`.

Reason:

- Prevents duplicate friends.
- Allows fast friendship checking using `contains`.
- Average lookup time is `O(1)`.

#### Example

```text
User u001:
name = Alice
workplace = Google
hometown = Dundee
friendIds = [u002, u003]
```

---

 ### 5.2 `SocialNetwork.java`

#### Responsibility

`SocialNetwork` stores the entire social network graph.

It owns the collection of all users and provides low-level graph operations such as adding users and creating friendships.

#### Class

```java
public class SocialNetwork
```

#### Core Fields

```java
private Map<String, User> users;
```

#### Optional Advanced Fields

```java
private Map<String, Set<String>> usersByHometown;
private Map<String, Set<String>> usersByWorkplace;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `users` | `Map<String, User>` | Maps user ID to the matching `User` object |
| `usersByHometown` | `Map<String, Set<String>>` | Optional index for fast hometown-based lookup |
| `usersByWorkplace` | `Map<String, Set<String>>` | Optional index for fast workplace-based lookup |

#### Key Methods

```java
public boolean addUser(User user)
public User getUser(String userId)
public boolean containsUser(String userId)
public Collection<User> getAllUsers()
public boolean addFriendship(String userId1, String userId2)
public boolean removeFriendship(String userId1, String userId2)
public void clear()
```

If advanced indexes are implemented:

```java
public Set<String> getUsersFromHometown(String hometown)
public Set<String> getUsersFromWorkplace(String workplace)
private void addToIndexes(User user)
private void removeFromIndexes(User user)
private void updateIndexesAfterProfileChange(User user, String oldHometown, String oldWorkplace)
```

#### Graph Representation

The network is represented as an adjacency list:

```text
users:
u001 -> User(Alice, friends=[u002, u003])
u002 -> User(Bob, friends=[u001])
u003 -> User(Carol, friends=[u001])
```

#### Friendship Direction

Friendship should be undirected. If `u001` adds `u002`, both users must be updated:

```text
u001.friendIds.add("u002")
u002.friendIds.add("u001")
```

#### Design Reason

`HashMap<String, User>` gives average `O(1)` lookup by user ID. This is much better than scanning an `ArrayList<User>`, which would be `O(n)`.

---

### 5.3 `Session.java`

#### Responsibility

`Session` stores the current login state.

It tells the system whether a user is logged in and which user is currently active.

#### Class

```java
public class Session
```

#### Fields

```java
private String currentUserId;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `currentUserId` | `String` | Stores the ID of the logged-in user; `null` means no user is logged in |

#### Key Methods

```java
public boolean isLoggedIn()
public String getCurrentUserId()
public void login(String userId)
public void logout()
```

#### Example State

Before login:

```text
currentUserId = null
```

After Alice logs in:

```text
currentUserId = "u001"
```

#### Design Reason

Using a `Session` means the user does not need to enter their own user ID for every operation. All profile, friend, and recommendation actions can be performed relative to

## 6. `service` Folder

The `service` folder contains business logic. These classes use the model classes but keep complex operations out of the UI.

---

### 6.1 `AuthService.java`

#### Responsibility

`AuthService` handles user registration, login, and logout.

#### Class

```java
public class AuthService
```

#### Fields

```java
private SocialNetwork network;
private Session session;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `network` | `SocialNetwork` | Used to find existing users or add new users |
| `session` | `Session` | Used to set or clear login state |

#### Key Methods

```java
public boolean register(String userId, String password, String name, String workplace, String hometown)
public boolean login(String userId, String password)
public void logout()
public boolean isUserIdTaken(String userId)
```

#### Registration Flow

```text
User enters userId, password, name, workplace, hometown
 ↓
AuthService checks whether userId already exists
 ↓
PasswordUtil hashes the password
 ↓
New User object is created
 ↓
User is added to SocialNetwork
 ↓
Session logs in the new user
```

#### Login Flow

```text
User enters userId and password
 ↓
AuthService finds the user in SocialNetwork
 ↓
PasswordUtil verifies password against stored passwordHash
 ↓
If valid, Session stores currentUserId
 ↓
User enters logged-in menu
```

#### Design Reason

Authentication logic is separated from the console menu, making the system easier to test and modify.

---

### 6.2 `UserService.java`

#### Responsibility

`UserService` handles user profile operations.

#### Class

```java
public class UserService
```

#### Fields

```java
private SocialNetwork network;
private Session session;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `network` | `SocialNetwork` | Used to retrieve or update users |
| `session` | `Session` | Used to identify the current logged-in user |

#### Key Methods

```java
public User getCurrentUser()
public boolean updateCurrentUserProfile(String name, String workplace, String hometown)
public Collection<User> getAllUsers()
public User getUserById(String userId)
```

If advanced indexes are implemented:

```java
public Set<User> findUsersByHometown(String hometown)
public Set<User> findUsersByWorkplace(String workplace)
```

#### Profile Update Flow

```text
ConsoleMenu asks for new profile details
 ↓
UserService gets currentUserId from Session
 ↓
UserService finds the User in SocialNetwork
 ↓
User fields are updated
 ↓
If indexes exist, SocialNetwork updates hometown/workplace indexes
```

#### Design Reason

Profile logic is kept separate from `User`, because `User` should mainly represent data, while `UserService` manages user-related operations.

---

### 6.3 `FriendService.java`

#### Responsibility

`FriendService` handles friendship operations and friend-related queries.

This class covers many core requirements from the assignment.

#### Class

```java
public class FriendService
```

#### Fields

```java
private SocialNetwork network;
private Session session;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `network` | `SocialNetwork` | Used to access users and friendship graph |
| `session` | `Session` | Used to identify the logged-in user |

#### Key Methods

```java
public List<User> getCurrentUserFriends()
public List<User> getFriendsOfUser(String userId)
public boolean addFriendToCurrentUser(String friendId)
public Set<User> getCommonFriends(String otherUserId)
public List<User> filterCurrentUserFriendsByHometown(String hometown)
public List<User> filterCurrentUserFriendsByWorkplace(String workplace)
public boolean areFriends(String userId1, String userId2)
```

#### Display Friends Flow

```text
ConsoleMenu requests current user's friends
 ↓
FriendService reads currentUserId from Session
 ↓
FriendService gets current User from SocialNetwork
 ↓
FriendService converts friendIds into User objects
 ↓
ConsoleMenu displays the result
```

#### View Friend's Friends Flow

```text
Current user selects a friend
 ↓
FriendService checks the selected user exists
 ↓
FriendService gets selected friend's friendIds
 ↓
FriendService converts those IDs into User objects
 ↓
ConsoleMenu displays the selected friend's friend list
```

#### Add Friend Flow

```text
Current user selects a user to add
 ↓
FriendService checks:
   - current user is logged in
   - target user exists
   - target is not current user
   - target is not already a friend
 ↓
SocialNetwork.addFriendship(currentUserId, targetUserId)
 ↓
Both users' friend sets are updated
```

#### Common Friends Flow

```text
Current user chooses another user
 ↓
FriendService gets both users' friendIds
 ↓
Creates a copy of the smaller set
 ↓
Keeps only IDs also contained in the other set
 ↓
Converts common friend IDs to User objects
 ↓
ConsoleMenu displays common friends
```

#### Design Reason

The friendship logic is graph logic. Keeping it in `FriendService` makes it easier to explain the system as a graph-based design.

---

### 6.4 `RecommendationService.java`

#### Responsibility

`RecommendationService` recommends possible new friends.

The recommendation should focus on friends-of-friends and shared attributes such as hometown or workplace.

#### Class

```java
public class RecommendationService
```

#### Fields

```java
private SocialNetwork network;
private Session session;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `network` | `SocialNetwork` | Used to access the graph |
| `session` | `Session` | Used to identify the current user |

#### Key Methods

```java
public List<User> recommendFriends()
public Map<User, Integer> recommendFriendsWithScores()
private int calculateRecommendationScore(User currentUser, User candidate)
```

#### Basic Recommendation Algorithm

```text
For each friend of the current user:
    For each friend of that friend:
        Ignore if candidate is the current user
        Ignore if candidate is already a friend
        Recommend if candidate has same hometown or workplace
```

#### Scored Recommendation Algorithm

A more advanced version can assign points:

```text
+2 points for same workplace
+2 points for same hometown
+1 point for each mutual friend
```

Then recommendations are sorted by highest score.

#### Recommendation Flow

```text
ConsoleMenu requests recommendations
 ↓
RecommendationService gets current user from Session
 ↓
Looks through friends-of-friends
 ↓
Filters invalid candidates
 ↓
Scores candidates
 ↓
Sorts candidates by score
 ↓
ConsoleMenu displays recommended users
```

#### Design Reason

This avoids scanning every user in the system. Instead, it focuses on the local graph around the current user, which is more efficient for large networks.

---

## 7. `persistence` Folder

The `persistence` folder handles file input and output.

---

### 7.1 `NetworkFileManager.java`

#### Responsibility

`NetworkFileManager` saves the whole social network to a file and loads it back.

It should not handle menu input, login logic, or friend recommendation logic.

#### Class

```java
public class NetworkFileManager
```

#### Fields

No permanent fields are strictly required.

Optional:

```java
private static final String USER_PREFIX = "USER";
private static final String FRIEND_PREFIX = "FRIEND";
```

#### Key Methods

```java
public SocialNetwork loadFromFile(String filePath)
public void saveToFile(SocialNetwork network, String filePath)
```

Optional helper methods:

```java
private User parseUserLine(String line)
private String formatUserLine(User user)
private String formatFriendLine(String userId1, String userId2)
```

#### Recommended File Format

```text
USER,u001,Alice,Google,Dundee,passwordHashHere
USER,u002,Bob,Amazon,London,passwordHashHere
USER,u003,Carol,Google,Dundee,passwordHashHere
FRIEND,u001,u002
FRIEND,u001,u003
```

#### Save Flow

```text
ConsoleMenu chooses Save
 ↓
NetworkFileManager receives SocialNetwork
 ↓
Writes every user line
 ↓
Writes every friendship line
 ↓
Avoids writing duplicate friendship edges
```

Because friendships are undirected, the system should avoid saving both:

```text
FRIEND,u001,u002
FRIEND,u002,u001
```

Only one edge should be saved.

#### Load Flow

```text
ConsoleMenu chooses Load
 ↓
NetworkFileManager reads file
 ↓
First pass: create all users
 ↓
Second pass: create all friendships
 ↓
Returns a complete SocialNetwork object
```

#### Why Two-Pass Loading Is Better

Friendship lines may refer to users that appear later in the file. A two-pass load avoids errors by creating all users before creating edges.

#### Design Reason

Separating file operations makes the program easier to test and keeps the service classes focused on business rules.

---

## 8. `ui` Folder

The `ui` folder contains the console user interface.

---

### 8.1 `ConsoleMenu.java`

#### Responsibility

`ConsoleMenu` handles all console interaction:

- Displaying menus.
- Reading user input.
- Calling the correct service method.
- Printing results.

It should not contain core algorithms.

#### Class

```java
public class ConsoleMenu
```

#### Fields

```java
private Scanner scanner;
private SocialNetwork network;
private Session session;
private AuthService authService;
private UserService userService;
private FriendService friendService;
private RecommendationService recommendationService;
private NetworkFileManager fileManager;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `scanner` | `Scanner` | Reads console input |
| `network` | `SocialNetwork` | Current network data |
| `session` | `Session` | Current login state |
| `authService` | `AuthService` | Handles register/login/logout |
| `userService` | `UserService` | Handles profile operations |
| `friendService` | `FriendService` | Handles friend operations |
| `recommendationService` | `RecommendationService` | Handles recommendations |
| `fileManager` | `NetworkFileManager` | Handles load/save |

#### Key Methods

```java
public void start()
private void showGuestMenu()
private void showUserMenu()
private void handleRegister()
private void handleLogin()
private void handleLogout()
private void handleViewProfile()
private void handleEditProfile()
private void handleDisplayFriends()
private void handleViewFriendsOfFriend()
private void handleAddFriend()
private void handleCommonFriends()
private void handleFilterFriends()
private void handleRecommendations()
private void handleLoad()
private void handleSave()
private String readRequiredText(String prompt)
```

#### Guest Menu

The guest menu is shown when no user is logged in:

```text
1. Register
2. Login
3. Load network from file
4. Save network to file
0. Exit
```

#### Logged-In User Menu

The user menu is shown after login:

```text
1. View my profile
2. Edit my profile
3. Display my friends
4. View a friend's friends
5. Add a friend
6. Show common friends
7. Filter my friends by hometown
8. Filter my friends by workplace
9. Show friend recommendations
10. Save network to file
11. Logout
0. Exit
```

#### Design Reason

The menu is separated into guest and logged-in states. This makes the interaction more realistic and prevents users from performing friend operations without logging in.

---

## 9. `util` Folder

The `util` folder contains helper classes that do not belong to a specific model or service.

---

### 9.1 `PasswordUtil.java`

#### Responsibility

`PasswordUtil` hashes and verifies passwords.

The system should store password hashes instead of plain-text passwords.

#### Class

```java
public class PasswordUtil
```

#### Fields

```java
private static final int SALT_LENGTH = 16;
private static final int ITERATIONS = 600_000;
private static final int KEY_LENGTH_BITS = 256;
```

#### Field Explanation

| Field | Type | Purpose |
|---|---|---|
| `SALT_LENGTH` | `int` | Length of the random salt in bytes |
| `ITERATIONS` | `int` | Number of PBKDF2 iterations |
| `KEY_LENGTH_BITS` | `int` | Length of the generated hash key in bits |

#### Key Methods

```java
public static String hashPassword(String password)
public static boolean verifyPassword(String password, String storedPasswordHash)
private static byte[] pbkdf2(String password, byte[] salt, int iterations)
```

#### Password Hashing Flow

```text
User enters password during registration
 ↓
PasswordUtil creates random salt
 ↓
PasswordUtil applies PBKDF2WithHmacSHA256
 ↓
Salt and hash are encoded as Base64
 ↓
Stored value is saved as
"pbkdf2-sha256$600000$<Base64 salt>$<Base64 hash>"
```

#### Password Verification Flow

```text
User enters password during login
 ↓
Stored salt is extracted
 ↓
Entered password is hashed with same salt
 ↓
New hash is compared with the stored hash using `MessageDigest.isEqual`
 ↓
Login succeeds only if hashes match
```

#### Design Reason

Storing plain-text passwords is poor practice. Even though this is a student project, using password hashing shows professional awareness of basic security design.

Legacy values produced by `String.hashCode()` are intentionally rejected and
must be replaced by registering the account again.

---

## 10. Overall Data Flow

### 10.1 Registration Data Flow

```text
ConsoleMenu
 ↓ user input
AuthService.register(...)
 ↓ checks user ID
PasswordUtil.hashPassword(...)
 ↓ creates password hash
User object
 ↓
SocialNetwork.addUser(...)
 ↓
Session.login(userId)
```

### 10.2 Login Data Flow

```text
ConsoleMenu
 ↓ user input
AuthService.login(...)
 ↓
SocialNetwork.getUser(userId)
 ↓
PasswordUtil.verifyPassword(...)
 ↓
Session.login(userId)
```

### 10.3 Friend Query Data Flow

```text
ConsoleMenu
 ↓
FriendService
 ↓
Session.getCurrentUserId()
 ↓
SocialNetwork.getUser(currentUserId)
 ↓
User.getFriendIds()
 ↓
SocialNetwork.getUser(friendId)
 ↓
ConsoleMenu displays users
```

### 10.4 File Save Data Flow

```text
ConsoleMenu
 ↓
NetworkFileManager.saveToFile(network, filePath)
 ↓
SocialNetwork.getAllUsers()
 ↓
Writes USER lines
 ↓
Writes FRIEND lines
```

### 10.5 File Load Data Flow

```text
ConsoleMenu
 ↓
NetworkFileManager.loadFromFile(filePath)
 ↓
Creates new SocialNetwork
 ↓
Reads USER lines
 ↓
Creates User objects
 ↓
Reads FRIEND lines
 ↓
Creates friendships
 ↓
ConsoleMenu replaces current network data
```

---

## 11. Overall Running Logic

The whole program runs as a loop.

```text
Program starts
 ↓
Main creates SocialNetworkApp
 ↓
SocialNetworkApp creates models, services, file manager, and menu
 ↓
ConsoleMenu.start()
 ↓
Is a user logged in?
 |-- No  -> show guest menu
 `-- Yes -> show logged-in user menu
 ↓
User selects an option
 ↓
ConsoleMenu calls the correct service
 ↓
Service updates or queries the model
 ↓
ConsoleMenu prints result
 ↓
Loop continues until user exits
```

---

## 12. Core Algorithms

### 12.1 Add Friendship

```text
Input: currentUserId, targetUserId

Check current user exists
Check target user exists
Check target is not current user
Check target is not already a friend
Add targetUserId to current user's friendIds
Add currentUserId to target user's friendIds
```

Complexity:

```text
Average O(1)
```

because `HashMap` lookup and `HashSet` insertion are average `O(1)`.

---

### 12.2 Common Friends

```text
Input: currentUserId, otherUserId

Get current user's friendIds
Get other user's friendIds
Copy the smaller set
Retain only IDs also found in the other set
Convert result IDs to User objects
```

Complexity:

```text
O(min(a, b))
```

where:

- `a` is the current user's number of friends.
- `b` is the other user's number of friends.

---

### 12.3 Filter Friends

```text
Input: currentUserId, hometown or workplace

Get current user's friends
For each friend:
    Compare hometown or workplace
Return matching users
```

Complexity:

```text
O(f)
```

where `f` is the current user's number of friends.

This is acceptable because it filters only the current user's friend list, not all users.

---

### 12.4 Friend Recommendations

```text
Input: currentUserId

Get current user
For each friend of current user:
    For each friend of that friend:
        Ignore current user
        Ignore existing friends
        Score candidate:
            +2 same hometown
            +2 same workplace
            +1 each mutual friend
Return candidates sorted by score
```

Complexity:

```text
O(f * k)
```

where:

- `f` is the current user's number of friends.
- `k` is the average number of friends each friend has.

This is better than scanning every user in the entire network.

---

## 13. Large-Scale User Design

The brief mentions considering how the design would cope with a very large number of users, such as 10 million users.

The design addresses this by avoiding full-network scans where possible.

### 13.1 Why `HashMap` Is Used

Using:

```java
Map<String, User> users = new HashMap<>();
```

allows fast user lookup by ID.

Average complexity:

```text
O(1)
```

Rejected alternative:

```java
List<User> users = new ArrayList<>();
```

This would require scanning users one by one.

Complexity:

```text
O(n)
```

For 10 million users, this would be inefficient.

### 13.2 Why `HashSet` Is Used

Using:

```java
Set<String> friendIds = new HashSet<>();
```

allows fast checks for whether two users are already friends.

Average complexity:

```text
O(1)
```

This is important for:

- Adding friends.
- Avoiding duplicate friendships.
- Checking recommendation candidates.
- Finding common friends.

### 13.3 Why Adjacency List Is Used

The graph is stored as an adjacency list:

```text
User ID -> Set of friend IDs
```

This only stores real friendships.

Rejected alternative:

```text
Adjacency matrix
```

An adjacency matrix would require:

```text
n * n
```

space.

For 10 million users:

```text
10,000,000 * 10,000,000
```

entries would be impossible to store realistically.

### 13.4 Optional Indexes

For even better large-scale performance, the system can maintain:

```java
Map<String, Set<String>> usersByHometown;
Map<String, Set<String>> usersByWorkplace;
```

These indexes allow faster lookup of users from the same hometown or workplace.

Example:

```text
Dundee -> [u001, u003, u010]
Google -> [u002, u006, u011]
```

These indexes are useful for advanced friend recommendation and filtering.

---

## 14. Report-Ready Architecture Summary

The system is designed as a layered Java console application. The social network is modelled as an undirected graph using an adjacency-list structure. A `HashMap<String, User>` stores all users and allows average `O(1)` lookup by user ID. Each `User` stores their friends in a `HashSet<String>`, which prevents duplicate friendships and allows average `O(1)` friendship checks.

The application separates responsibilities into model, service, persistence, utility, and UI layers. The model classes store the core data. The service classes contain business logic such as authentication, profile management, friend operations, and recommendations. The persistence class handles saving and loading the network from disk. The console menu handles user interaction only and delegates real work to the services.

The login system uses a `Session` object to track the currently authenticated user. Passwords are stored as hashes rather than plain text, using a utility class for password hashing and verification. This improves the professional quality of the design.

For scalability, the system avoids scanning the entire user base for common operations. User lookup uses a hash map, friend checks use hash sets, common-friend detection uses set intersection, and recommendations are generated by checking friends-of-friends rather than all users. Optional hometown and workplace indexes can be added to support efficient large-scale filtering and recommendation.

---

## 15. Suggested Implementation Order

1. Implement `User`.
2. Implement `SocialNetwork`.
3. Implement `Session`.
4. Implement `PasswordUtil`.
5. Implement `AuthService`.
6. Implement `UserService`.
7. Implement `FriendService`.
8. Implement `RecommendationService`.
9. Implement `NetworkFileManager`.
10. Implement `ConsoleMenu`.
11. Connect everything in `SocialNetworkApp`.
12. Start the program from `Main`.
13. Add sample data file.
14. Create test plan.
15. Write report sections based on this architecture.

---

## 16. Minimum Feature Checklist

| Requirement | Covered By |
|---|---|
| Register user | `AuthService`, `User`, `PasswordUtil` |
| Login/logout | `AuthService`, `Session` |
| View/edit profile | `UserService`, `User` |
| Store all users | `SocialNetwork` |
| Store friend relationships | `User.friendIds`, `SocialNetwork` |
| Display friends | `FriendService` |
| View friend's friends | `FriendService` |
| Add friend from another user's friend list | `FriendService` |
| Display common friends | `FriendService` |
| Filter friends by hometown/workplace | `FriendService` |
| Friend recommendations | `RecommendationService` |
| Save network to file | `NetworkFileManager` |
| Load network from file | `NetworkFileManager` |
| Usable menu | `ConsoleMenu` |
| Large-scale design discussion | `HashMap`, `HashSet`, adjacency list, optional indexes |
