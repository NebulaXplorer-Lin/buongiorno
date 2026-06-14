package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.User;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

public class DashboardController implements AppController {
    @FXML
    private Label loggedInUserLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button friendsButton;

    @FXML
    private Button recommendationsButton;

    @FXML
    private Button loadButton;

    @FXML
    private TabPane contentTabPane;

    @FXML
    private Tab profileTab;

    @FXML
    private Tab friendsTab;

    @FXML
    private Tab recommendationsTab;

    private SocialNetworkFxApp app;
    private AppContext context;

    @Override
    public void setApp(SocialNetworkFxApp app, AppContext context) {
        this.app = app;
        this.context = context;
        refreshLoggedInUser();
    }

    @FXML
    private void initialize() {
        profileButton.setOnAction(event -> selectTab(profileTab));
        friendsButton.setOnAction(event -> selectTab(friendsTab));
        recommendationsButton.setOnAction(event -> selectTab(recommendationsTab));
        saveButton.setOnAction(event -> showInfo("Save is not implemented yet."));
        loadButton.setOnAction(event -> showInfo("Load is not implemented yet."));
        logoutButton.setOnAction(event -> handleLogout());
    }

    private void refreshLoggedInUser() {
        String currentUserId = context.getSession().getCurrentUserId();
        if (currentUserId == null) {
            loggedInUserLabel.setText("Not signed in");
            return;
        }

        User currentUser = context.getNetwork().getUser(currentUserId);
        if (currentUser == null) {
            loggedInUserLabel.setText("Not signed in");
            return;
        }

        loggedInUserLabel.setText(currentUser.getUserName() + " (" + currentUser.getUserId() + ")");
    }

    private void selectTab(Tab tab) {
        contentTabPane.getSelectionModel().select(tab);
    }

    private void handleLogout() {
        context.getAuthService().logout();

        try {
            app.showLoginScreen();
        } catch (Exception exception) {
            showError("Could not return to login screen.");
        }
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Dashboard Error", message);
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
