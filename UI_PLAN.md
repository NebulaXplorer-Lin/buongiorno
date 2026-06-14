# JavaFX UI Plan

## 1. Goal

This document describes how to change the current command-line / console UI design into a **JavaFX graphical user interface**.

The core system design should remain the same:

- `SocialNetwork` stores the whole social network graph.
- `User` represents one user node.
- `friendIds` stores each user's adjacent friendship connections.
- `AuthService`, `UserService`, `FriendService`, and `RecommendationService` contain business logic.
- `NetworkFileManager` handles loading and saving data.
- The UI layer should only display screens, collect input, call service methods, and refresh the view.

The GUI should replace `ConsoleMenu`, not replace the whole architecture.

## 2. Why Use JavaFX

JavaFX is suitable if the project wants a more modern and visually clear GUI than a console menu or basic Swing interface.

Advantages:

- It provides modern UI controls such as `TableView`, `ListView`, `TabPane`, `Dialog`, `FileChooser`, and layout containers.
- It supports CSS styling, so the interface can look cleaner and more professional.
- It separates UI layout and logic well if FXML is used.
- It is good for building a dashboard-style application with login, profile, friends, and recommendations screens.

Disadvantages:

- JavaFX may require extra SDK configuration depending on the Java version.
- If using Java 11 or later, JavaFX is usually not included in the JDK by default.
- The project may need Maven, Gradle, or manual module-path setup.
- It is slightly more complex than Swing for a small coursework project.

Decision:

Use JavaFX if the team has enough time to configure and test the environment. The data structure and service design will stay unchanged.

## 3. Difficulty

Overall difficulty: **medium to medium-high**.

The GUI part is not algorithmically difficult, but JavaFX adds some setup complexity.

Main sources of difficulty:

- configuring JavaFX SDK or Maven dependencies;
- connecting controllers to services cleanly;
- refreshing `TableView` / `ListView` after data changes;
- keeping the same `SocialNetwork` object after loading from file;
- ensuring unfinished services are completed before GUI screens call them.

If services are already complete, JavaFX is manageable. If services are still incomplete, the GUI will be blocked by missing business methods.

## 4. Architecture Change

Current planned console flow:

```text
Main
 -> SocialNetworkApp
 -> ConsoleMenu
 -> Services
 -> Model
```

New JavaFX flow:

```text
Main
 -> SocialNetworkApp
 -> JavaFX Application / MainFrame
 -> Controllers
 -> Services
 -> Model
```

The important change is that `ConsoleMenu` is replaced by JavaFX screens and controllers.

In a console application, the program waits in a loop for text commands. In JavaFX, the program waits for UI events such as button clicks, table selections, text input, and file chooser actions.

## 5. Recommended JavaFX Structure

Recommended package structure:

```text
src/
 |-- Main.java
 |-- app/
 |   `-- SocialNetworkApp.java
 |-- model/
 |-- service/
 |-- persistence/
 |-- ui/
 |   `-- javafx/
 |       |-- SocialNetworkFxApp.java
 |       |-- AppContext.java
 |       |-- controller/
 |       |   |-- LoginController.java
 |       |   |-- RegisterController.java
 |       |   |-- DashboardController.java
 |       |   |-- ProfileController.java
 |       |   |-- FriendsController.java
 |       |   `-- RecommendationsController.java
 |       |-- view/
 |       |   |-- login.fxml
 |       |   |-- register.fxml
 |       |   |-- dashboard.fxml
 |       |   |-- profile.fxml
 |       |   |-- friends.fxml
 |       |   `-- recommendations.fxml
 |       `-- style/
 |           `-- app.css
 `-- util/
```

If the team wants a simpler version, all screens can be created directly in Java code without FXML. However, for a report, FXML makes the UI architecture easier to explain because layout and event logic are separated.

## 6. JavaFX Setup Options

### Option A: Maven

This is the cleanest option if the team is allowed to use Maven.

Add JavaFX dependencies in `pom.xml`, for example:

```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.2</version>
    </dependency>
</dependencies>
```

This makes JavaFX easier to run consistently across different machines.

### Option B: Manual JavaFX SDK

Download JavaFX SDK and run with module path:

```text
--module-path path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

This works, but every teammate must configure the SDK path correctly.

### Recommended Setup

Use Maven if possible. If the project must stay as a simple `src` folder, use manual JavaFX SDK configuration.

## 7. Required Code Changes

### 7.1 `Session.java`

JavaFX controllers need to know whether a user is logged in and who the current user is.

Required methods:

```java
public boolean isLoggedIn()
public String getCurrentUserId()
```

Current code has `isLoggedin()`. It should either be renamed to `isLoggedIn()` or both methods should be kept for compatibility.

### 7.2 `Main.java`

There are two possible designs.

Option 1: Start JavaFX directly:

```java
public class Main {
    public static void main(String[] args) {
        Application.launch(SocialNetworkFxApp.class, args);
    }
}
```

Option 2: Keep `SocialNetworkApp` as the composition root and let JavaFX receive the created services through an `AppContext`.

The second option is better for preserving the existing architecture.

### 7.3 `SocialNetworkApp.java`

`SocialNetworkApp` should still create:

```java
private SocialNetwork network;
private Session session;
private AuthService authService;
private UserService userService;
private FriendService friendService;
private RecommendationService recommendationService;
private NetworkFileManager fileManager;
```

Instead of creating `ConsoleMenu`, it should create or provide an `AppContext` object that JavaFX controllers can use.

Example:

```java
public AppContext createContext() {
    return new AppContext(
        network,
        session,
        authService,
        userService,
        friendService,
        recommendationService,
        fileManager
    );
}
```

### 7.4 `AppContext.java`

`AppContext` is a small helper class used to pass shared services into controllers.

It can store:

```java
private SocialNetwork network;
private Session session;
private AuthService authService;
private UserService userService;
private FriendService friendService;
private RecommendationService recommendationService;
private NetworkFileManager fileManager;
```

This avoids creating services repeatedly in every controller.

### 7.5 Service Layer

JavaFX should not directly edit `HashMap`, `HashSet`, or user fields from the UI.

Controllers should call service methods:

```java
AuthService.register(...)
AuthService.login(...)
AuthService.logout()

UserService.getCurrentUser()
UserService.updateCurrentUserProfile(...)
UserService.getAllUsers()
UserService.getUserById(...)

FriendService.getCurrentUserFriends()
FriendService.getFriendsOfUser(...)
FriendService.addFriendToCurrentUser(...)
FriendService.getCommonFriends(...)
FriendService.filterCurrentUserFriendsByHometown(...)
FriendService.filterCurrentUserFriendsByWorkplace(...)

RecommendationService.recommendFriends()
RecommendationService.recommendFriendsWithScores()

NetworkFileManager.loadFromFile(...)
NetworkFileManager.saveToFile(...)
```

## 8. JavaFX Screen Plan

### 8.1 Login Screen

FXML:

```text
login.fxml
```

Controls:

- `TextField userIdField`
- `PasswordField passwordField`
- `Button loginButton`
- `Button registerButton`
- `Button loadButton`
- `Button saveButton`
- `Button exitButton`

Flow:

```text
User enters ID/password
 -> clicks Login
 -> LoginController calls authService.login(...)
 -> if true, switch to dashboard.fxml
 -> if false, show Alert error
```

### 8.2 Register Screen

FXML:

```text
register.fxml
```

Controls:

- `TextField userIdField`
- `PasswordField passwordField`
- `TextField nameField`
- `TextField workplaceField`
- `TextField hometownField`
- `Button registerButton`
- `Button cancelButton`

Flow:

```text
User enters registration details
 -> RegisterController calls authService.register(...)
 -> if true, switch to dashboard.fxml
 -> if false, show duplicate user ID error
```

### 8.3 Dashboard Screen

FXML:

```text
dashboard.fxml
```

Suggested layout:

- top area: logged-in user ID/name, save button, logout button;
- left navigation: Profile, Friends, Recommendations;
- center area: content panel or `TabPane`.

Recommended JavaFX controls:

- `BorderPane`
- `VBox`
- `Button`
- `TabPane`
- `Tab`

The dashboard replaces the logged-in console menu.

### 8.4 Profile Screen

FXML:

```text
profile.fxml
```

Displays:

- user ID;
- name;
- workplace;
- hometown.

Actions:

- edit name;
- edit workplace;
- edit hometown;
- save profile.

Calls:

```java
userService.getCurrentUser()
userService.updateCurrentUserProfile(...)
```

### 8.5 Friends Screen

FXML:

```text
friends.fxml
```

Recommended controls:

- `TableView<User>` for friend list;
- `TextField friendIdField` for adding a friend;
- `TextField filterField` for hometown/workplace filtering;
- buttons for add friend, view friend's friends, common friends, filter, refresh.

Columns:

- user ID;
- name;
- workplace;
- hometown.

Actions:

- display current friends;
- add friend;
- view selected friend's friends;
- show common friends with selected or entered user;
- filter friends by hometown;
- filter friends by workplace.

Calls:

```java
friendService.getCurrentUserFriends()
friendService.addFriendToCurrentUser(...)
friendService.getFriendsOfUser(...)
friendService.getCommonFriends(...)
friendService.filterCurrentUserFriendsByHometown(...)
friendService.filterCurrentUserFriendsByWorkplace(...)
```

### 8.6 Recommendations Screen

FXML:

```text
recommendations.fxml
```

Recommended controls:

- `TableView` for recommended users;
- score column if using scored recommendations;
- refresh button;
- add selected recommendation button.

Columns:

- user ID;
- name;
- workplace;
- hometown;
- score.

Calls:

```java
recommendationService.recommendFriends()
recommendationService.recommendFriendsWithScores()
friendService.addFriendToCurrentUser(...)
```

### 8.7 Save / Load

Use JavaFX `FileChooser`.

Save:

```java
fileManager.saveToFile(network, selectedFile.getPath());
```

Load:

```java
SocialNetwork loaded = fileManager.loadFromFile(selectedFile.getPath());
```

Important:

After loading, the GUI and services must use the loaded data correctly.

Recommended simple approach:

- load data into a temporary `SocialNetwork`;
- clear the existing `network`;
- copy loaded users and friendships into the existing `network`.

This avoids breaking existing references in services and controllers.

## 9. Controller Responsibilities

Controllers should be thin.

They should:

- read values from JavaFX controls;
- validate required fields;
- call the correct service method;
- show success or error messages using `Alert`;
- refresh `TableView` / `ListView`;
- switch scenes or tabs.

Controllers should not:

- calculate recommendations directly;
- manually manipulate friendship sets;
- parse save files;
- implement graph algorithms.

Those responsibilities belong to model, service, and persistence classes.

## 10. Data Flow Examples

### Login

```text
LoginController
 -> authService.login(userId, password)
 -> Session stores currentUserId
 -> DashboardController loads current user
 -> dashboard.fxml is shown
```

### Add Friend

```text
FriendsController
 -> reads friendIdField
 -> friendService.addFriendToCurrentUser(friendId)
 -> SocialNetwork.addFriendship(currentUserId, friendId)
 -> TableView refreshes friend list
```

### Recommendation

```text
RecommendationsController
 -> recommendationService.recommendFriendsWithScores()
 -> service checks friends-of-friends
 -> controller displays result in TableView
```

### Save

```text
DashboardController
 -> FileChooser selects path
 -> NetworkFileManager.saveToFile(network, path)
 -> Alert confirms success
```

## 11. Implementation Order

Recommended order:

1. Finish model consistency:
   - `Session.getCurrentUserId()`
   - `Session.isLoggedIn()`
   - align `User.getUserName()` and architecture naming

2. Finish service classes:
   - `UserService`
   - `FriendService`
   - `RecommendationService`

3. Finish persistence:
   - `NetworkFileManager.saveToFile(...)`
   - `NetworkFileManager.loadFromFile(...)`

4. Configure JavaFX:
   - choose Maven or manual JavaFX SDK;
   - confirm a simple JavaFX window runs.

5. Create `AppContext`.

6. Create JavaFX application entry:
   - `SocialNetworkFxApp`
   - first stage
   - scene switching helper

7. Build login and register screens.

8. Build dashboard screen.

9. Build profile screen.

10. Build friends screen.

11. Build recommendations screen.

12. Add save/load with `FileChooser`.

13. Test all workflows.

14. Apply CSS styling after functions are stable.

## 12. Testing Checklist

Manual test cases:

- Open JavaFX application successfully.
- Register a new user.
- Duplicate user ID registration fails.
- Login with correct password.
- Login with wrong password fails.
- Logout returns to login screen.
- View current profile.
- Edit profile and confirm updated values appear.
- Add existing user as friend.
- Cannot add self as friend.
- Cannot add duplicate friend.
- Display current user's friends in table.
- View selected friend's friends.
- Show common friends.
- Filter friends by hometown.
- Filter friends by workplace.
- Show friend recommendations.
- Add recommended user as friend.
- Save network to file.
- Restart app and load file.
- Loaded users and friendships are correct.

## 13. Risk Areas

### Risk 1: JavaFX environment setup

If JavaFX is not configured correctly, the program may not run even if the code is correct.

Solution:

Use Maven if possible, or document the exact JavaFX SDK path and VM options.

### Risk 2: Controllers become too large

If one controller handles every screen, the GUI code becomes hard to maintain.

Solution:

Use separate controllers for login, register, dashboard, profile, friends, and recommendations.

### Risk 3: Services not finished

JavaFX screens depend on service methods. If service classes are empty, buttons cannot work.

Solution:

Finish and test services before connecting all GUI actions.

### Risk 4: Loading a file breaks object references

If `loadFromFile` returns a new `SocialNetwork`, existing controllers may still point to the old one.

Solution:

Either copy loaded data into the existing network, or recreate the `AppContext`, services, and controllers after load.

### Risk 5: UI contains business logic

If graph algorithms are written in controllers, the design becomes harder to explain and test.

Solution:

Keep graph logic in service/model classes. Controllers only coordinate UI events.

## 14. Report-Ready Summary

The GUI version of the project uses JavaFX to replace the original console menu with a graphical interface. The application's core layered architecture remains unchanged. The model layer still represents the social network as an adjacency-list graph using `HashMap<String, User>` and `HashSet<String>` for efficient lookup and friendship checks. The service layer still handles authentication, profile operations, friendship queries, and friend recommendations. The persistence layer still handles file loading and saving.

JavaFX is used only in the UI layer. Screens such as login, registration, profile management, friends, and recommendations are implemented using JavaFX views and controllers. Controllers respond to user actions, call service methods, and update JavaFX controls such as `TableView`, `TextField`, and `Alert`.

This design keeps the graphical interface separate from the data structure and algorithms. As a result, the project can provide a more user-friendly GUI while still preserving a clear, testable, and reportable architecture.

## 15. Final Recommendation

Using JavaFX is a good choice if the team wants a more modern GUI and can handle the extra setup. The safest implementation path is:

```text
finish services
 -> configure JavaFX
 -> create AppContext
 -> build login/register
 -> build dashboard
 -> connect profile/friends/recommendations
 -> add save/load
 -> style with CSS
```

The most important design point is that JavaFX should replace only the UI layer. The core graph structure and service algorithms should remain independent from the GUI.
