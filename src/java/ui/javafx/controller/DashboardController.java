package ui.javafx.controller;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
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
    private Button logoutButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button friendsButton;

    @FXML
    private Button recommendationsButton;

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
        loadTabContent();
    }

    @FXML
    private void initialize() {
        profileButton.setOnAction(event -> selectTab(profileTab));
        friendsButton.setOnAction(event -> selectTab(friendsTab));
        recommendationsButton.setOnAction(event -> selectTab(recommendationsTab));
        logoutButton.setOnAction(event -> handleLogout());
    }

    private void refreshLoggedInUser() {
        User currentUser = context.getUserService().getCurrentUser();
        if (currentUser == null) {
            loggedInUserLabel.setText("Not signed in");
            return;
        }

        loggedInUserLabel.setText(currentUser.getUserName() + " (" + currentUser.getUserId() + ")");
    }

    private void selectTab(Tab tab) {
        contentTabPane.getSelectionModel().select(tab);
    }

    private void loadTabContent() {
        profileTab.setContent(loadChildView("/ui/javafx/view/profile.fxml"));
        friendsTab.setContent(loadChildView("/ui/javafx/view/friends.fxml"));
        recommendationsTab.setContent(loadChildView("/ui/javafx/view/recommendations.fxml"));
    }

    private Parent loadChildView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof AppController appController) {
                appController.setApp(app, context);
            }

            return root;
        } catch (Exception exception) {
            showError("Could not load " + fxmlPath + ".");
            return new Label("Could not load screen.");
        }
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
