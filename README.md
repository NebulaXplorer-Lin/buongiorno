# Buongiorno Social Network

Buongiorno is a desktop social networking application developed with **Java 21, JavaFX, and Maven** for the DI12010 Introduction to Data Structures and Algorithms group project.

The application models a social network as an undirected graph: users are vertices and friendships are edges. It uses an adjacency-list representation and a layered architecture for authentication, profile management, friendship queries, recommendations, and file persistence.

## Features

- User registration, login, and logout
- Password storage using salted PBKDF2-HMAC-SHA256 hashes
- View and edit name, workplace, and hometown
- Search users by user ID, name, workplace, or hometown
- Add users as friends
- Remove users from the current user's friend list
- Display the current user's friends
- View a selected friend's friends
- Find common friends
- Filter displayed users by hometown and workplace
- Generate scored friend recommendations
- Import and export users and friendship data
- JavaFX interface built with FXML and CSS

## Technology Stack

| Technology | Version or Purpose |
| --- | --- |
| Java | 21 |
| JavaFX | 21.0.2 |
| Maven | Dependency management, building, and execution |
| JUnit | 5.11.4 |
| FXML and CSS | Interface layout and styling |

## Getting Started

### Requirements

Install the following software:

- JDK 21
- Maven 3.9 or a compatible version

Verify the installation:

```bash
java -version
mvn -version
```

### Run the Application

Run the following command from the project root:

```bash
mvn clean javafx:run
```

The application starts with an empty network. On the login screen, either:

1. Select **Register** to create a user; or
2. Select **Load** to import an existing network data file.

Successful registration automatically signs in the new user and opens the dashboard.

### Build and Test

```bash
mvn clean test
mvn clean package
```

The current automated tests cover password hashing, password verification, random salt generation, stored-hash validation, and invalid input handling.

## User Interface

The login screen provides login, registration, data import, data export, and exit actions.

After login, the dashboard contains three main areas:

- **Profile** — view and edit personal information.
- **Friends** — search for users, add or remove friends, inspect friendship relationships, find common friends, and filter users.
- **Recommendations** — view recommended users and their scores, then add a selected recommendation as a friend.

## Friend Recommendation Algorithm

Recommendation candidates are selected from friends of the current user's friends. The current user and existing friends are excluded.

Each candidate receives the following score:

| Condition | Score |
| --- | ---: |
| Same workplace as the current user | +2 |
| Same hometown as the current user | +2 |
| Each mutual friend | +1 |

Recommendations are ordered by descending score. The interface uses the user ID as the secondary ordering criterion when scores are equal.

## Data Structures and Complexity

The central data structures are:

```java
Map<String, User> users;
Set<String> friendIds;
Map<String, Set<String>> usersByWorkplace;
Map<String, Set<String>> usersByHometown;
```

- `HashMap` provides average `O(1)` user lookup by ID.
- `HashSet` prevents duplicate friendships and provides average `O(1)` lookup and insertion.
- Friendships are undirected, so adding an edge updates both users.
- Workplace and hometown indexes support efficient attribute-based searches.
- Common friends are calculated using set intersection in approximately `O(min(a, b))`, where `a` and `b` are the two users' friend counts.
- Recommendations inspect the local friends-of-friends graph instead of scanning every user.

## Data File Format

The application uses a comma-separated text format. Each record begins with either `USER` or `FRIEND`:

```text
USER,userId,userName,workplace,hometown,passwordHash
FRIEND,userId1,userId2
```

Example:

```text
USER,u001,Alice,OpenAI,Dundee,pbkdf2-sha256$600000$<salt>$<hash>
USER,u002,Bob,Google,London,pbkdf2-sha256$600000$<salt>$<hash>
FRIEND,u001,u002
```

Each undirected friendship is written only once. During loading, all users are created before friendship records are processed, so friendship records can safely refer to users defined later in the file.

Data files included in the repository:

- `doc/initial_network.txt` contains initial network data using the current PBKDF2 password format.
- `doc/sample_network.txt` contains legacy numeric password hashes that are no longer accepted by the current authentication implementation. It should only be used as a reference for the older data format.

Passwords must not be stored as plain text. The current stored format is:

```text
pbkdf2-sha256$iterations$Base64Salt$Base64Hash
```

## Project Structure

```text
.
├── pom.xml
├── src
│   ├── java
│   │   ├── app            # Entry point and object composition
│   │   ├── model          # User, SocialNetwork, and Session
│   │   ├── persistence    # Network loading and saving
│   │   ├── service        # Authentication, user, friend, and recommendation logic
│   │   ├── ui/javafx      # JavaFX application, FXML controllers, views, and styles
│   │   └── util           # Password hashing utility
│   └── test               # JUnit tests
└── doc
    ├── architecture.md
    ├── architecture_english.md
    ├── class-diagram.md
    ├── UI_PLAN.md
    └── *.txt              # Example network data
```

## Architecture

```text
Main
  -> SocialNetworkFxApp
  -> JavaFX Controllers
  -> Service Layer
  -> Model and Persistence
```

- `app.Main` launches the JavaFX application.
- `SocialNetworkApp` creates the network, session, services, and file manager.
- `ui.javafx.AppContext` shares the same application objects with all controllers.
- `model` stores users, session state, graph relationships, and search indexes.
- `service` contains business rules and graph algorithms.
- `persistence` serializes and restores the network.
- `ui.javafx` handles presentation, input validation, and navigation.

Further documentation:

- [Architecture](doc/architecture.md)
- [English Architecture](doc/architecture_english.md)
- [Class Diagram](doc/class-diagram.md)
- [JavaFX UI Plan](doc/UI_PLAN.md)

## Author

- Zhehan Lin — 2717301@dundee.ac.uk — [@NebulaXplorer-Lin](https://github.com/NebulaXplorer-Lin)
