package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.User;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

public class ProfileController implements AppController {
    @FXML
    private Label userIdLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField workplaceField;

    @FXML
    private TextField hometownField;

    @FXML
    private Button editNameButton;

    @FXML
    private Button editWorkplaceButton;

    @FXML
    private Button editHometownButton;

    @FXML
    private Button saveProfileButton;

    private AppContext context;

    @Override
    public void setApp(SocialNetworkFxApp app, AppContext context) {
        this.context = context;
        refreshProfile();
    }

    @FXML
    private void initialize() {
        editNameButton.setOnAction(event -> nameField.requestFocus());
        editWorkplaceButton.setOnAction(event -> workplaceField.requestFocus());
        editHometownButton.setOnAction(event -> hometownField.requestFocus());
        saveProfileButton.setOnAction(event -> handleSaveProfile());
    }

    private void refreshProfile() {
        User currentUser = context.getUserService().getCurrentUser();
        if (currentUser == null) {
            userIdLabel.setText("-");
            nameField.clear();
            workplaceField.clear();
            hometownField.clear();
            return;
        }

        userIdLabel.setText(currentUser.getUserId());
        nameField.setText(currentUser.getUserName());
        workplaceField.setText(currentUser.getWorkplace());
        hometownField.setText(currentUser.getHometown());
    }

    private void handleSaveProfile() {
        String name = nameField.getText().trim();
        String workplace = workplaceField.getText().trim();
        String hometown = hometownField.getText().trim();

        if (name.isEmpty() || workplace.isEmpty() || hometown.isEmpty()) {
            showError("Name, workplace, and hometown cannot be empty.");
            return;
        }

        boolean updateSucceeded = context.getUserService()
                .updateCurrentUserProfile(name, workplace, hometown);

        if (!updateSucceeded) {
            showError("No signed-in user found.");
            return;
        }

        refreshProfile();
        showInfo("Profile updated.");
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Profile Error", message);
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
