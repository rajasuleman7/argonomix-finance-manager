package com.argonomix.controllers;

import com.argonomix.database.GoalDAO;
import com.argonomix.models.Goal;
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

public class GoalController implements Initializable {
    
    @FXML private VBox goalsContainer;
    @FXML private VBox completedGoalsContainer;
    
    private GoalDAO goalDAO;
    private ObservableList<Goal> goals = FXCollections.observableArrayList();
    
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
        
        goalDAO = new GoalDAO();
        loadGoals();
    }
    
    private void loadGoals() {
        try {
            int userId = SessionManager.getCurrentUserId();
            List<Goal> goalList = goalDAO.findByUserId(userId);
            goals.setAll(goalList);
            updateGoalDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateGoalDisplay() {
        goalsContainer.getChildren().clear();
        completedGoalsContainer.getChildren().clear();
        
        List<Goal> activeGoals = goals.stream()
            .filter(g -> !g.isCompleted())
            .collect(java.util.stream.Collectors.toList());
        
        List<Goal> completedGoals = goals.stream()
            .filter(Goal::isCompleted)
            .collect(java.util.stream.Collectors.toList());
        
        for (Goal goal : activeGoals) {
            VBox goalCard = createGoalCard(goal);
            goalsContainer.getChildren().add(goalCard);
        }
        
        for (Goal goal : completedGoals) {
            VBox goalCard = createGoalCard(goal);
            completedGoalsContainer.getChildren().add(goalCard);
        }
        
        if (activeGoals.isEmpty()) {
            Label noGoalsLabel = new Label("No active goals. Create your first savings goal!");
            noGoalsLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
            goalsContainer.getChildren().add(noGoalsLabel);
        }
    }
    
    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 20px;");
        
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(goal.getGoalIcon() != null ? goal.getGoalIcon() : "🎯");
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        VBox info = new VBox(5);
        Label nameLabel = new Label(goal.getGoalName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label amountLabel = new Label(String.format("$%.2f / $%.2f", 
            goal.getCurrentAmount(), goal.getTargetAmount()));
        amountLabel.setStyle("-fx-text-fill: #6b7280;");
        
        info.getChildren().addAll(nameLabel, amountLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label percentageLabel = new Label(String.format("%.0f%%", goal.getProgressPercentage()));
        percentageLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #9333ea;");
        
        header.getChildren().addAll(iconLabel, info, spacer, percentageLabel);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(goal.getProgressPercentage() / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);
        progressBar.setStyle("-fx-accent: #9333ea;");
        
        HBox actions = new HBox(10);
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> handleEditGoal(goal));
        editButton.setStyle("-fx-text-fill: #9333ea;");
        
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> handleDeleteGoal(goal));
        deleteButton.setStyle("-fx-text-fill: #ef4444;");
        
        Button addMoneyButton = new Button("Add Money");
        addMoneyButton.setOnAction(e -> handleAddMoney(goal));
        addMoneyButton.getStyleClass().add("primary-button");
        
        actions.getChildren().addAll(editButton, deleteButton, addMoneyButton);
        
        card.getChildren().addAll(header, progressBar, actions);
        return card;
    }
    
    @FXML
    private void handleCreateGoal() {
        showGoalModal(null);
    }
    
    private void handleEditGoal(Goal goal) {
        showGoalModal(goal);
    }
    
    private void handleDeleteGoal(Goal goal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Goal");
        confirm.setHeaderText("Are you sure you want to delete this goal?");
        confirm.setContentText("This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                goalDAO.delete(goal.getGoalId());
                loadGoals();
            } catch (Exception e) {
                showError("Error deleting goal: " + e.getMessage());
            }
        }
    }
    
    private void handleAddMoney(Goal goal) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Add Money to Goal");
        dialog.setHeaderText("Add Money to " + goal.getGoalName());
        dialog.setContentText("Amount:");
        
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                goal.setCurrentAmount(goal.getCurrentAmount() + amount);
                
                if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
                    goal.setCompleted(true);
                }
                
                goalDAO.update(goal);
                loadGoals();
            } catch (NumberFormatException e) {
                showError("Invalid amount");
            } catch (Exception e) {
                showError("Error updating goal: " + e.getMessage());
            }
        });
    }
    
    private void showGoalModal(Goal goal) {
        // TODO: Implement goal modal
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(goal == null ? "Create Goal" : "Edit Goal");
        alert.setHeaderText("Goal Form");
        alert.setContentText("Goal modal will be implemented here.");
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


