package ui.javafx;

import app.SocialNetworkApp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.javafx.controller.AppController;

public class SocialNetworkFxApp extends Application {
    private AppContext context;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        SocialNetworkApp app = new SocialNetworkApp();
        context = app.createContext();

        showLoginScreen();

        primaryStage.setTitle("Buongiorno Social Network");
        primaryStage.show();
    }

    public void showLoginScreen() throws Exception {
        setScene("/ui/javafx/view/login.fxml");
    }

    public void showRegisterScreen() throws Exception {
        setScene("/ui/javafx/view/register.fxml");
    }

    public void showDashboardScreen() throws Exception {
        setScene("/ui/javafx/view/dashboard.fxml");
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

    public AppContext getContext() {
        return context;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
