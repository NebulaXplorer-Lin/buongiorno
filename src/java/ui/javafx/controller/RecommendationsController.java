package ui.javafx.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.User;
import ui.javafx.AppContext;
import ui.javafx.SocialNetworkFxApp;

/**
 * Controls display and acceptance of scored friend recommendations.
 */
public class RecommendationsController implements AppController {
    @FXML
    private Button refreshButton;

    @FXML
    private Button addSelectedButton;

    @FXML
    private TableView<RecommendationRow> recommendationsTable;

    @FXML
    private TableColumn<RecommendationRow, String> userIdColumn;

    @FXML
    private TableColumn<RecommendationRow, String> nameColumn;

    @FXML
    private TableColumn<RecommendationRow, String> workplaceColumn;

    @FXML
    private TableColumn<RecommendationRow, String> hometownColumn;

    @FXML
    private TableColumn<RecommendationRow, Number> scoreColumn;

    private AppContext context;

    /**
     * Creates a recommendations controller for use by the FXML loader.
     */
    public RecommendationsController() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApp(SocialNetworkFxApp app, AppContext context) {
        this.context = context;
        refreshRecommendations();
    }

    @FXML
    private void initialize() {
        userIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser().getUserId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser().getUserName()));
        workplaceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser().getWorkplace()));
        hometownColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser().getHometown()));
        scoreColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getScore()));

        refreshButton.setOnAction(event -> refreshRecommendations());
        addSelectedButton.setOnAction(event -> handleAddSelected());
    }

    private void refreshRecommendations() {
        recommendationsTable.setItems(FXCollections.observableArrayList(calculateRecommendations()));
    }

    private void handleAddSelected() {
        RecommendationRow selectedRow = recommendationsTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            showError("Please select a recommendation.");
            return;
        }

        boolean addSucceeded = context.getFriendService()
                .addFriendToCurrentUser(selectedRow.getUser().getUserId());

        if (!addSucceeded) {
            showError("Could not add selected recommendation.");
            return;
        }

        refreshRecommendations();
        showInfo("Friend added.");
    }

    private List<RecommendationRow> calculateRecommendations() {
        Map<User, Integer> scores = context.getRecommendationService().recommendFriendsWithScores();
        return scores.entrySet().stream()
                .map(entry -> new RecommendationRow(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(RecommendationRow::getScore).reversed()
                        .thenComparing(row -> row.getUser().getUserId()))
                .toList();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Recommendations Error", message);
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

    /**
     * Immutable table-row model containing a recommended user and score.
     */
    public static class RecommendationRow {
        private final User user;
        private final int score;

        /**
         * Creates a recommendation row.
         *
         * @param user recommended user
         * @param score recommendation score
         */
        public RecommendationRow(User user, int score) {
            this.user = user;
            this.score = score;
        }

        /**
         * Returns the recommended user.
         *
         * @return the recommended user
         */
        public User getUser() {
            return user;
        }

        /**
         * Returns the recommendation score.
         *
         * @return the recommendation score
         */
        public int getScore() {
            return score;
        }
    }
}
