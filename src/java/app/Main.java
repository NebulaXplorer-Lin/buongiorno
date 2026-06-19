package app;

import javafx.application.Application;
import ui.javafx.SocialNetworkFxApp;

/**
 * Provides the command-line entry point for the Buongiorno application.
 */
public class Main {
    private Main() {
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(SocialNetworkFxApp.class, args);
    }
}
