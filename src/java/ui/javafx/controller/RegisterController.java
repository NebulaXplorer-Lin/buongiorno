package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

public class RegisterController implements AppController {
    @FXML
    private TextField userIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField workplaceField;

    @FXML
    private TextField hometownField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button registerButton;

    private SocialNetworkFxApp app;
    private AppContext context;

    @Override
    public void setApp(SocialNetworkFxApp app, AppContext context) {
        this.app = app;
        this.context = context;
    }

    @FXML
    private void initialize() {
        cancelButton.setOnAction(event -> handleCancel());
        registerButton.setOnAction(event -> handleRegister());
    }

    private void handleRegister() {
        String userId = userIdField.getText().trim();
        String password = passwordField.getText();
        String name = nameField.getText().trim();
        String workplace = workplaceField.getText().trim();
        String hometown = hometownField.getText().trim();

        if (userId.isEmpty() || password.isEmpty() || name.isEmpty()
                || workplace.isEmpty() || hometown.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        boolean registerSucceeded = context.getAuthService()
                .register(userId, password, name, workplace, hometown);

        if (!registerSucceeded) {
            showError("This user ID is already taken.");
            return;
        }

        try {
            app.showDashboardScreen();
        } catch (Exception exception) {
            showError("Could not open dashboard screen.");
        }
    }

    private void handleCancel() {
        try {
            app.showLoginScreen();
        } catch (Exception exception) {
            showError("Could not return to login screen.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Register Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
