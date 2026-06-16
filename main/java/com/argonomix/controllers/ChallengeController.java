package com.argonomix.controllers;

import com.argonomix.database.ChallengeDAO;
import com.argonomix.models.Challenge;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ChallengeController implements Initializable {
    
    @FXML private VBox challengesContainer;
    @FXML private VBox completedChallengesContainer;
    
    private ChallengeDAO challengeDAO;
    private ObservableList<Challenge> challenges = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isLoggedIn()) {
            try {
                NavigationUtil.switchToScene("/fxml/Login.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        challengeDAO = new ChallengeDAO();
        loadChallenges();
    }
    
    private void loadChallenges() {
        try {
            int userId = SessionManager.getCurrentUserId();
            List<Challenge> challengeList = challengeDAO.findByUserId(userId);
            challenges.setAll(challengeList);
            updateChallengeDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateChallengeDisplay() {
        challengesContainer.getChildren().clear();
        completedChallengesContainer.getChildren().clear();
        
        List<Challenge> activeChallenges = challenges.stream()
            .filter(c -> !c.isCompleted())
            .collect(java.util.stream.Collectors.toList());
        
        List<Challenge> completedChallenges = challenges.stream()
            .filter(Challenge::isCompleted)
            .collect(java.util.stream.Collectors.toList());
        
        for (Challenge challenge : activeChallenges) {
            VBox challengeCard = createChallengeCard(challenge);
            challengesContainer.getChildren().add(challengeCard);
        }
        
        for (Challenge challenge : completedChallenges) {
            VBox challengeCard = createChallengeCard(challenge);
            completedChallengesContainer.getChildren().add(challengeCard);
        }
        
        if (activeChallenges.isEmpty()) {
            Label noChallengesLabel = new Label("No active challenges. Create your first savings challenge!");
            noChallengesLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
            challengesContainer.getChildren().add(noChallengesLabel);
        }
    }
    
    private VBox createChallengeCard(Challenge challenge) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 20px;");
        
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        VBox info = new VBox(5);
        Label nameLabel = new Label(challenge.getChallengeName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label typeLabel = new Label(challenge.getChallengeType());
        typeLabel.setStyle("-fx-text-fill: #6b7280;");
        
        info.getChildren().addAll(nameLabel, typeLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label progressLabel = new Label(String.format("%d/%d days", 
            challenge.getDaysCompleted(), challenge.getTotalDays()));
        progressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #9333ea;");
        
        header.getChildren().addAll(info, spacer, progressLabel);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(challenge.getProgressPercentage() / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);
        progressBar.setStyle("-fx-accent: #9333ea;");
        
        Label amountSavedLabel = new Label(String.format("Amount Saved: $%.2f", challenge.getAmountSaved()));
        amountSavedLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        
        HBox actions = new HBox(10);
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> handleEditChallenge(challenge));
        editButton.setStyle("-fx-text-fill: #9333ea;");
        
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> handleDeleteChallenge(challenge));
        deleteButton.setStyle("-fx-text-fill: #ef4444;");
        
        actions.getChildren().addAll(editButton, deleteButton);
        
        card.getChildren().addAll(header, progressBar, amountSavedLabel, actions);
        return card;
    }
    
    @FXML
    private void handleCreateChallenge() {
        showChallengeModal(null);
    }
    
    private void handleEditChallenge(Challenge challenge) {
        showChallengeModal(challenge);
    }
    
    private void handleDeleteChallenge(Challenge challenge) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Challenge");
        confirm.setHeaderText("Are you sure you want to delete this challenge?");
        confirm.setContentText("This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                challengeDAO.delete(challenge.getChallengeId());
                loadChallenges();
            } catch (Exception e) {
                showError("Error deleting challenge: " + e.getMessage());
            }
        }
    }
    
    private void showChallengeModal(Challenge challenge) {
        // TODO: Implement challenge modal
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(challenge == null ? "Create Challenge" : "Edit Challenge");
        alert.setHeaderText("Challenge Form");
        alert.setContentText("Challenge modal will be implemented here.");
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


