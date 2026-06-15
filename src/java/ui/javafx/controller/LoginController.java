package ui.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;
import model.SocialNetwork;
import model.User;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

public class LoginController implements AppController {
    @FXML
    private TextField userIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label userLookupLabel;

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
        userIdField.textProperty().addListener((observable, oldValue, newValue) -> updateUserLookupLabel());
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
        loadButton.setOnAction(event -> handleLoad());
        saveButton.setOnAction(event -> handleSave());
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

    private void handleLoad() {
        File selectedFile = createDataFileChooser("Load Network Data")
                .showOpenDialog(app.getPrimaryStage());

        if (selectedFile == null) {
            return;
        }

        try {
            SocialNetwork loadedNetwork = context.getFileManager().loadFromFile(selectedFile.getPath());
            context.replaceCurrentNetworkData(loadedNetwork);
            context.getSession().logout();
            updateUserLookupLabel();
            showInfo("Network data loaded. Please sign in.");
        } catch (Exception exception) {
            showError("Could not load network data.");
        }
    }

    private void updateUserLookupLabel() {
        if (context == null) {
            return;
        }

        String userId = userIdField.getText().trim();
        if (userId.isEmpty()) {
            userLookupLabel.setText("");
            userLookupLabel.getStyleClass().removeAll("lookup-found", "lookup-missing");
            return;
        }

        User user = context.getUserService().getUserById(userId);
        userLookupLabel.getStyleClass().removeAll("lookup-found", "lookup-missing");
        if (user == null) {
            userLookupLabel.setText("User not found");
            userLookupLabel.getStyleClass().add("lookup-missing");
        } else {
            userLookupLabel.setText(user.getUserName());
            userLookupLabel.getStyleClass().add("lookup-found");
        }
    }

    private void handleSave() {
        File selectedFile = createDataFileChooser("Save Network Data")
                .showSaveDialog(app.getPrimaryStage());

        if (selectedFile == null) {
            return;
        }

        try {
            context.getFileManager().saveToFile(context.getNetwork(), selectedFile.getPath());
            showInfo("Network data saved.");
        } catch (Exception exception) {
            showError("Could not save network data.");
        }
    }

    private FileChooser createDataFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        return fileChooser;
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
