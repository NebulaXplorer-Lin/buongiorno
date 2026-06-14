package ui.javafx.controller;

import java.util.Collection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.User;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

public class FriendsController implements AppController {
    @FXML
    private TextField friendIdField;

    @FXML
    private TextField filterField;

    @FXML
    private Button addFriendButton;

    @FXML
    private Button viewFriendsButton;

    @FXML
    private Button commonFriendsButton;

    @FXML
    private Button filterHometownButton;

    @FXML
    private Button filterWorkplaceButton;

    @FXML
    private Button refreshButton;

    @FXML
    private TableView<User> friendsTable;

    @FXML
    private TableColumn<User, String> userIdColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> workplaceColumn;

    @FXML
    private TableColumn<User, String> hometownColumn;

    private AppContext context;

    @Override
    public void setApp(SocialNetworkFxApp app, AppContext context) {
        this.context = context;
        refreshFriends();
    }

    @FXML
    private void initialize() {
        userIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        workplaceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkplace()));
        hometownColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHometown()));

        addFriendButton.setOnAction(event -> handleAddFriend());
        viewFriendsButton.setOnAction(event -> handleViewFriends());
        commonFriendsButton.setOnAction(event -> handleCommonFriends());
        filterHometownButton.setOnAction(event -> handleFilterByHometown());
        filterWorkplaceButton.setOnAction(event -> handleFilterByWorkplace());
        refreshButton.setOnAction(event -> handleReset());
    }

    private void refreshFriends() {
        showUsers(context.getFriendService().getCurrentUserFriends());
    }

    private void handleReset() {
        friendIdField.clear();
        filterField.clear();
        friendsTable.getSelectionModel().clearSelection();
        refreshFriends();
    }

    private void handleAddFriend() {
        String friendId = friendIdField.getText().trim();
        if (friendId.isEmpty()) {
            showError("Please enter a friend user ID.");
            return;
        }

        User currentUser = context.getUserService().getCurrentUser();
        if (currentUser == null) {
            showError("Please sign in before adding friends.");
            return;
        }

        if (currentUser.getUserId().equals(friendId)) {
            showError("You cannot add yourself as a friend.");
            return;
        }

        User targetUser = context.getUserService().getUserById(friendId);
        if (targetUser == null) {
            showError("No user found with ID: " + friendId);
            return;
        }

        if (context.getFriendService().areFriends(currentUser.getUserId(), friendId)) {
            showError(targetUser.getUserName() + " is already your friend.");
            return;
        }

        boolean addSucceeded = context.getFriendService().addFriendToCurrentUser(friendId);
        if (!addSucceeded) {
            showError("Could not add friend.");
            return;
        }

        friendIdField.clear();
        refreshFriends();
        showInfo(targetUser.getUserName() + " added as a friend.");
    }

    private void handleViewFriends() {
        User selectedUser = getSelectedOrEnteredUser();
        if (selectedUser == null) {
            showError("Select a user or enter a user ID.");
            return;
        }

        showUsers(context.getFriendService().getFriendsOfUser(selectedUser.getUserId()));
    }

    private void handleCommonFriends() {
        User otherUser = getSelectedOrEnteredUser();
        if (otherUser == null) {
            showError("Select a user or enter a user ID.");
            return;
        }

        showUsers(context.getFriendService().getCommonFriends(otherUser.getUserId()));
    }

    private void handleFilterByHometown() {
        String hometown = filterField.getText().trim();
        if (hometown.isEmpty()) {
            showError("Please enter a hometown filter.");
            return;
        }

        showUsers(context.getFriendService().filterCurrentUserFriendsByHometown(hometown));
    }

    private void handleFilterByWorkplace() {
        String workplace = filterField.getText().trim();
        if (workplace.isEmpty()) {
            showError("Please enter a workplace filter.");
            return;
        }

        showUsers(context.getFriendService().filterCurrentUserFriendsByWorkplace(workplace));
    }

    private User getSelectedOrEnteredUser() {
        User selectedUser = friendsTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            return selectedUser;
        }

        String userId = friendIdField.getText().trim();
        if (userId.isEmpty()) {
            return null;
        }

        return context.getUserService().getUserById(userId);
    }

    private void showUsers(Collection<User> users) {
        friendsTable.setItems(FXCollections.observableArrayList(users));
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Friends Error", message);
    }

    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
