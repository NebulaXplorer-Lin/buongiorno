package ui.javafx;

import app.SocialNetworkApp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ui.javafx.controller.AppController;

/**
 * JavaFX application responsible for stage setup and screen navigation.
 */
public class SocialNetworkFxApp extends Application {
    private AppContext context;
    private Stage primaryStage;

    /**
     * Creates the JavaFX application instance.
     */
    public SocialNetworkFxApp() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        SocialNetworkApp app = new SocialNetworkApp();
        context = app.createContext();

        showLoginScreen();

        primaryStage.setTitle("Buongiorno Social Network");
        primaryStage.show();
        showWelcomeDialog();
    }

    private void showWelcomeDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("Welcome");
        alert.setHeaderText("Welcome to Buongiorno Social Network!");
        alert.setContentText(
                "[2] sample test data files are available in the [/sample] folder for testing.\n"
                        + "The password for all users is: 123456");
        alert.showAndWait();
    }

    /**
     * Displays the login screen and sizes the stage to its content.
     *
     * @throws Exception if the FXML view cannot be loaded
     */
    public void showLoginScreen() throws Exception {
        setScene("/ui/javafx/view/login.fxml");
        primaryStage.setMaximized(false);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    /**
     * Displays the registration screen.
     *
     * @throws Exception if the FXML view cannot be loaded
     */
    public void showRegisterScreen() throws Exception {
        setScene("/ui/javafx/view/register.fxml");
    }

    /**
     * Displays the dashboard and maximizes the stage.
     *
     * @throws Exception if the FXML view cannot be loaded
     */
    public void showDashboardScreen() throws Exception {
        setScene("/ui/javafx/view/dashboard.fxml");
        primaryStage.setMaximized(true);
    }

    private void setScene(String fxmlPath) throws Exception {
        Parent root = loadView(fxmlPath);
        primaryStage.setScene(new Scene(root));
    }

    private Parent loadView(String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Object controller = loader.getController();

        if (controller instanceof AppController appController) {
            appController.setApp(this, context);
        }

        return root;
    }

    /**
     * Returns the shared application context.
     *
     * @return the shared application context
     */
    public AppContext getContext() {
        return context;
    }

    /**
     * Returns the primary JavaFX stage.
     *
     * @return the primary JavaFX stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
