package ui.javafx.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.User;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

/**
 * Controls friend listing, searching, filtering, relationship inspection, and
 * add/remove operations.
 */
public class FriendsController implements AppController {
    private static final String ALL_HOMETOWNS = "All Hometowns";
    private static final String ALL_WORKPLACES = "All Workplaces";

    private enum TableMode {
        CURRENT_FRIENDS,
        SEARCH_RESULTS,
        FRIENDS_OF_FRIEND,
        COMMON_FRIENDS
    }

    @FXML
    private ComboBox<String> searchTypeComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button addFriendButton;

    @FXML
    private Button removeFriendButton;

    @FXML
    private Label selectedFriendLabel;

    @FXML
    private Button viewFriendsButton;

    @FXML
    private Button commonFriendsButton;

    @FXML
    private Button detailsResetButton;

    @FXML
    private ComboBox<String> hometownFilterComboBox;

    @FXML
    private ComboBox<String> workplaceFilterComboBox;

    @FXML
    private Button sameHometownButton;

    @FXML
    private Button sameWorkplaceButton;

    @FXML
    private Button resetButton;

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
    private Collection<User> baseUsers = new ArrayList<>();
    private TableMode tableMode = TableMode.CURRENT_FRIENDS;
    private User selectedFriendForDetails;
    private boolean updatingFilterControls;

    /**
     * Creates a friends controller for use by the FXML loader.
     */
    public FriendsController() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApp(SocialNetworkFxApp app, AppContext context) {
        this.context = context;
        refreshFriends();
    }

    @FXML
    private void initialize() {
        userIdColumn.setCellValueFactory(data -> new SimpleStringProperty(valueOrBlank(data.getValue().getUserId())));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(valueOrBlank(data.getValue().getUserName())));
        workplaceColumn.setCellValueFactory(data -> new SimpleStringProperty(valueOrBlank(data.getValue().getWorkplace())));
        hometownColumn.setCellValueFactory(data -> new SimpleStringProperty(valueOrBlank(data.getValue().getHometown())));

        searchTypeComboBox.setItems(FXCollections.observableArrayList("User ID", "Name", "Workplace", "Hometown"));
        searchTypeComboBox.getSelectionModel().selectFirst();

        searchButton.setOnAction(event -> handleSearch());
        searchField.setOnAction(event -> handleSearch());
        addFriendButton.setOnAction(event -> handleAddFriend());
        removeFriendButton.setOnAction(event -> handleRemoveFriend());
        viewFriendsButton.setOnAction(event -> handleViewFriends());
        commonFriendsButton.setOnAction(event -> handleCommonFriends());
        detailsResetButton.setOnAction(event -> handleDetailsReset());
        hometownFilterComboBox.setOnAction(event -> handleFilterChange());
        workplaceFilterComboBox.setOnAction(event -> handleFilterChange());
        sameHometownButton.setOnAction(event -> handleSameHometown());
        sameWorkplaceButton.setOnAction(event -> handleSameWorkplace());
        resetButton.setOnAction(event -> handleReset());
        refreshButton.setOnAction(event -> handleRefresh());

        friendsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldUser, selectedUser) -> {
            if (tableMode == TableMode.CURRENT_FRIENDS) {
                selectedFriendForDetails = selectedUser;
                updateSelectedFriendLabel();
            }
            updateRemoveFriendButton();
        });

        updateSelectedFriendLabel();
        updateRemoveFriendButton();
    }

    private void refreshFriends() {
        selectedFriendForDetails = null;
        updateSelectedFriendLabel();
        showBaseUsers(context.getFriendService().getCurrentUserFriends(), TableMode.CURRENT_FRIENDS);
    }

    private void handleSearch() {
        String searchType = searchTypeComboBox.getValue();
        String searchValue = searchField.getText().trim();
        if (searchValue.isEmpty()) {
            showError("Please enter a search value.");
            return;
        }

        if (context.getUserService().getCurrentUser() == null) {
            showError("Please sign in before searching users.");
            return;
        }

        List<User> searchResults = new ArrayList<>(context.getUserService().searchUsers(searchType, searchValue));

        showBaseUsers(searchResults, TableMode.SEARCH_RESULTS);
        if (searchResults.isEmpty()) {
            showInfo("No users found.");
        }
    }

    private void handleReset() {
        updateFilterOptions(baseUsers, ALL_HOMETOWNS, ALL_WORKPLACES);
        showUsers(baseUsers);
    }

    private void handleRefresh() {
        searchField.clear();
        clearFilterSelections();
        friendsTable.getSelectionModel().clearSelection();
        refreshFriends();
    }

    private void handleAddFriend() {
        User targetUser = friendsTable.getSelectionModel().getSelectedItem();
        if (targetUser == null) {
            showError("Search for a user, then select one from the table.");
            return;
        }

        User currentUser = context.getUserService().getCurrentUser();
        if (currentUser == null) {
            showError("Please sign in before adding friends.");
            return;
        }

        String friendId = targetUser.getUserId();
        if (currentUser.getUserId().equals(friendId)) {
            showError("You cannot add yourself as a friend.");
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

        searchField.clear();
        refreshFriends();
        showInfo(targetUser.getUserName() + " added as a friend.\n\n" + formatUserProfile(targetUser));
    }

    private void handleRemoveFriend() {
        if (tableMode == TableMode.SEARCH_RESULTS) {
            showError("Friends cannot be removed from search results.");
            return;
        }

        User targetFriend = friendsTable.getSelectionModel().getSelectedItem();
        if (targetFriend == null) {
            showError("Please select a friend to remove.");
            return;
        }

        User currentUser = context.getUserService().getCurrentUser();
        if (currentUser == null
                || !context.getFriendService().areFriends(currentUser.getUserId(), targetFriend.getUserId())) {
            showError("You can only remove users who are currently your friends.");
            return;
        }

        TableMode removalMode = tableMode;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Friend");
        confirmation.setHeaderText("Remove this friend?");
        confirmation.setContentText(targetFriend.getUserName()
                + " ("
                + targetFriend.getUserId()
                + ") will be removed from your friend list.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        boolean removeSucceeded = context.getFriendService()
                .removeFriendFromCurrentUser(targetFriend.getUserId());
        if (!removeSucceeded) {
            showError("Could not remove friend.");
            return;
        }

        if (removalMode == TableMode.FRIENDS_OF_FRIEND) {
            showFriendsOfSelectedFriend();
            showWarning(
                    "Friend removed, but still shown",
                    targetFriend.getUserName()
                            + " has been removed from your friend list.\n\n"
                            + "However, this user is still a friend of "
                            + selectedFriendForDetails.getUserName()
                            + ", so they remain visible in this Friend's Friends list.");
        } else if (removalMode == TableMode.COMMON_FRIENDS) {
            showCommonFriendsWithSelectedFriend();
            showInfo(targetFriend.getUserName() + " removed from your friends.");
        } else {
            refreshFriends();
            showInfo(targetFriend.getUserName() + " removed from your friends.");
        }
    }

    private void handleViewFriends() {
        User targetFriend = getSelectedFriendForDetails();
        if (targetFriend == null) {
            return;
        }

        showFriendsOfSelectedFriend();
    }

    private void handleCommonFriends() {
        User targetFriend = getSelectedFriendForDetails();
        if (targetFriend == null) {
            return;
        }

        showCommonFriendsWithSelectedFriend();
    }

    private void showFriendsOfSelectedFriend() {
        showBaseUsers(
                context.getFriendService().getFriendsOfUser(selectedFriendForDetails.getUserId()),
                TableMode.FRIENDS_OF_FRIEND);
    }

    private void showCommonFriendsWithSelectedFriend() {
        showBaseUsers(
                context.getFriendService().getCommonFriends(selectedFriendForDetails.getUserId()),
                TableMode.COMMON_FRIENDS);
    }

    private void handleDetailsReset() {
        if (selectedFriendForDetails == null) {
            return;
        }

        friendsTable.getSelectionModel().clearSelection();
        refreshFriends();
    }

    private User getSelectedFriendForDetails() {
        if (selectedFriendForDetails == null) {
            showError("Please refresh to your main friend list and select one friend first.");
            return null;
        }

        return selectedFriendForDetails;
    }

    private void handleFilterChange() {
        if (updatingFilterControls) {
            return;
        }

        Collection<User> filteredUsers = new ArrayList<>();
        String hometown = hometownFilterComboBox.getValue();
        String workplace = workplaceFilterComboBox.getValue();

        for (User user : baseUsers) {
            boolean hometownMatches = isAllHometowns(hometown) || hometown.equals(user.getHometown());
            boolean workplaceMatches = isAllWorkplaces(workplace) || workplace.equals(user.getWorkplace());
            if (hometownMatches && workplaceMatches) {
                filteredUsers.add(user);
            }
        }

        showUsers(filteredUsers);
        updateFilterOptions(filteredUsers, hometown, workplace);
    }

    private void handleSameHometown() {
        User currentUser = context.getUserService().getCurrentUser();
        if (currentUser == null) {
            showError("Please sign in before filtering friends.");
            return;
        }

        String hometown = currentUser.getHometown();
        if (hometown == null || hometown.trim().isEmpty()) {
            showError("Your hometown is empty.");
            return;
        }

        if (!hometownFilterComboBox.getItems().contains(hometown)) {
            showInfo("No users in this list share your hometown.");
            return;
        }

        hometownFilterComboBox.getSelectionModel().select(hometown);
        handleFilterChange();
    }

    private void handleSameWorkplace() {
        User currentUser = context.getUserService().getCurrentUser();
        if (currentUser == null) {
            showError("Please sign in before filtering friends.");
            return;
        }

        String workplace = currentUser.getWorkplace();
        if (workplace == null || workplace.trim().isEmpty()) {
            showError("Your workplace is empty.");
            return;
        }

        if (!workplaceFilterComboBox.getItems().contains(workplace)) {
            showInfo("No users in this list share your workplace.");
            return;
        }

        workplaceFilterComboBox.getSelectionModel().select(workplace);
        handleFilterChange();
    }

    private void showUsers(Collection<User> users) {
        friendsTable.setItems(FXCollections.observableArrayList(sortUsers(users)));
    }

    private void showBaseUsers(Collection<User> users, TableMode mode) {
        tableMode = mode;
        baseUsers = new ArrayList<>(users);
        updateFilterOptions(baseUsers, ALL_HOMETOWNS, ALL_WORKPLACES);
        showUsers(baseUsers);
        friendsTable.getSelectionModel().clearSelection();
        updateRemoveFriendButton();
    }

    private void clearFilterSelections() {
        updatingFilterControls = true;
        hometownFilterComboBox.getSelectionModel().select(ALL_HOMETOWNS);
        workplaceFilterComboBox.getSelectionModel().select(ALL_WORKPLACES);
        updatingFilterControls = false;
    }

    private void updateFilterOptions(Collection<User> users, String selectedHometown, String selectedWorkplace) {
        TreeSet<String> hometowns = new TreeSet<>();
        TreeSet<String> workplaces = new TreeSet<>();

        for (User user : users) {
            addNonBlank(hometowns, user.getHometown());
            addNonBlank(workplaces, user.getWorkplace());
        }

        updatingFilterControls = true;
        List<String> hometownOptions = new ArrayList<>();
        hometownOptions.add(ALL_HOMETOWNS);
        hometownOptions.addAll(hometowns);

        List<String> workplaceOptions = new ArrayList<>();
        workplaceOptions.add(ALL_WORKPLACES);
        workplaceOptions.addAll(workplaces);

        hometownFilterComboBox.setItems(FXCollections.observableArrayList(hometownOptions));
        workplaceFilterComboBox.setItems(FXCollections.observableArrayList(workplaceOptions));
        selectFilterValue(hometownFilterComboBox, selectedHometown, hometownOptions, ALL_HOMETOWNS);
        selectFilterValue(workplaceFilterComboBox, selectedWorkplace, workplaceOptions, ALL_WORKPLACES);
        updatingFilterControls = false;
    }

    private void selectFilterValue(
            ComboBox<String> comboBox,
            String selectedValue,
            List<String> options,
            String defaultValue) {
        if (selectedValue != null && options.contains(selectedValue)) {
            comboBox.getSelectionModel().select(selectedValue);
        } else {
            comboBox.getSelectionModel().select(defaultValue);
        }
    }

    private List<User> sortUsers(Collection<User> users) {
        return users.stream()
                .sorted(Comparator.comparing(User::getUserId))
                .toList();
    }

    private void updateSelectedFriendLabel() {
        if (selectedFriendLabel == null) {
            return;
        }

        if (selectedFriendForDetails == null) {
            selectedFriendLabel.setText("Selected friend: -");
            return;
        }

        selectedFriendLabel.setText("Selected friend: "
                + selectedFriendForDetails.getUserName()
                + " ("
                + selectedFriendForDetails.getUserId()
                + ")");
    }

    private void updateRemoveFriendButton() {
        if (removeFriendButton == null || friendsTable == null) {
            return;
        }

        User selectedUser = friendsTable.getSelectionModel().getSelectedItem();
        User currentUser = context == null ? null : context.getUserService().getCurrentUser();
        boolean supportsRemoval = tableMode == TableMode.CURRENT_FRIENDS
                || tableMode == TableMode.FRIENDS_OF_FRIEND
                || tableMode == TableMode.COMMON_FRIENDS;
        boolean isCurrentFriend = selectedUser != null
                && currentUser != null
                && context.getFriendService().areFriends(currentUser.getUserId(), selectedUser.getUserId());
        boolean canRemoveFriend = supportsRemoval && isCurrentFriend;
        removeFriendButton.setDisable(!canRemoveFriend);
        removeFriendButton.setVisible(canRemoveFriend);
        removeFriendButton.setManaged(canRemoveFriend);
    }

    private void addNonBlank(TreeSet<String> values, String value) {
        if (value != null && !value.trim().isEmpty()) {
            values.add(value);
        }
    }

    private String valueOrBlank(String value) {
        return value != null ? value : "";
    }

    private boolean isAllHometowns(String value) {
        return value == null || ALL_HOMETOWNS.equals(value);
    }

    private boolean isAllWorkplaces(String value) {
        return value == null || ALL_WORKPLACES.equals(value);
    }

    private String formatUserProfile(User user) {
        return "User ID: " + user.getUserId()
                + "\nName: " + user.getUserName()
                + "\nWorkplace: " + user.getWorkplace()
                + "\nHometown: " + user.getHometown();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Friends Error", message);
    }

    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message);
    }

    private void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
