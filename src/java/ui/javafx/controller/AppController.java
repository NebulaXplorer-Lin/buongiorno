package ui.javafx.controller;

import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

/**
 * Contract implemented by controllers that receive application-level
 * dependencies after their FXML view is loaded.
 */
public interface AppController {
    /**
     * Supplies the JavaFX application and shared context.
     *
     * @param app JavaFX application
     * @param context shared application context
     */
    void setApp(SocialNetworkFxApp app, AppContext context);
}
