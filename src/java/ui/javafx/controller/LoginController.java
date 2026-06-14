package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

public class LoginController implements AppController {
    @FXML
    private TextField userIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button exitButton;

    private SocialNetworkFxApp app;
    private AppContext context;

    @Override
    public void setApp(SocialNetworkFxApp app, AppContext context) {
        this.app = app;
        this.context = context;
    }

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
        loadButton.setOnAction(event -> showInfo("Load is not implemented yet."));
        saveButton.setOnAction(event -> showInfo("Save is not implemented yet."));
        exitButton.setOnAction(event -> handleExit());
    }

    private void handleLogin() {
        String userId = userIdField.getText().trim();
        String password = passwordField.getText();

        if (userId.isEmpty() || password.isEmpty()) {
            showError("Please enter both user ID and password.");
            return;
        }

        boolean loginSucceeded = context.getAuthService().login(userId, password);
        if (!loginSucceeded) {
            showError("Invalid user ID or password.");
            return;
        }

        try {
            app.showDashboardScreen();
        } catch (Exception exception) {
            showError("Could not open dashboard screen.");
        }
    }

    private void handleRegister() {
        try {
            app.showRegisterScreen();
        } catch (Exception exception) {
            showError("Could not open register screen.");
        }
    }

    private void handleExit() {
        app.getPrimaryStage().close();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Login Error", message);
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
