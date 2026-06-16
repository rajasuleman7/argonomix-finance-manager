package com.argonomix.controllers;

import com.argonomix.database.SubscriptionDAO;
import com.argonomix.database.TransactionDAO;
import com.argonomix.models.Subscription;
import com.argonomix.models.Transaction;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SubscriptionController implements Initializable {
    
    @FXML private VBox subscriptionsContainer;
    @FXML private Label totalMonthlyLabel;
    @FXML private Label totalAnnualLabel;
    @FXML private Label percentOfIncomeLabel;
    
    private SubscriptionDAO subscriptionDAO;
    private TransactionDAO transactionDAO;
    private ObservableList<Subscription> subscriptions = FXCollections.observableArrayList();
    
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
        
        subscriptionDAO = new SubscriptionDAO();
        transactionDAO = new TransactionDAO();
        loadSubscriptions();
    }
    
    private void loadSubscriptions() {
        try {
            int userId = SessionManager.getCurrentUserId();
            List<Subscription> subscriptionList = subscriptionDAO.findByUserId(userId);
            subscriptions.setAll(subscriptionList);
            updateSubscriptionDisplay();
            updateSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateSubscriptionDisplay() {
        subscriptionsContainer.getChildren().clear();
        
        for (Subscription subscription : subscriptions) {
            VBox subscriptionCard = createSubscriptionCard(subscription);
            subscriptionsContainer.getChildren().add(subscriptionCard);
        }
        
        if (subscriptions.isEmpty()) {
            Label noSubsLabel = new Label("No subscriptions tracked. Add your first subscription!");
            noSubsLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
            subscriptionsContainer.getChildren().add(noSubsLabel);
        }
    }
    
    private VBox createSubscriptionCard(Subscription subscription) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 20px;");
        
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        VBox info = new VBox(5);
        Label nameLabel = new Label(subscription.getServiceName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label costLabel = new Label(String.format("$%.2f/month • $%.2f/year", 
            subscription.getMonthlyCost(), subscription.getAnnualCost()));
        costLabel.setStyle("-fx-text-fill: #6b7280;");
        
        if (subscription.getNextBillingDate() != null) {
            Label nextBillingLabel = new Label("Next billing: " + 
                subscription.getNextBillingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            nextBillingLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            info.getChildren().add(nextBillingLabel);
        }
        
        info.getChildren().addAll(nameLabel, costLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        ToggleButton statusToggle = new ToggleButton();
        statusToggle.setText(subscription.getStatus() == Subscription.SubscriptionStatus.KEEP ? "Keep" : "Consider Canceling");
        statusToggle.setSelected(subscription.getStatus() == Subscription.SubscriptionStatus.CONSIDER_CANCELING);
        statusToggle.setOnAction(e -> {
            Subscription.SubscriptionStatus newStatus = statusToggle.isSelected() ? 
                Subscription.SubscriptionStatus.CONSIDER_CANCELING : Subscription.SubscriptionStatus.KEEP;
            subscription.setStatus(newStatus);
            try {
                subscriptionDAO.update(subscription);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        if (subscription.getStatus() == Subscription.SubscriptionStatus.CONSIDER_CANCELING) {
            statusToggle.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
        }
        
        HBox actions = new HBox(10);
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> handleEditSubscription(subscription));
        editButton.setStyle("-fx-text-fill: #9333ea;");
        
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> handleDeleteSubscription(subscription));
        deleteButton.setStyle("-fx-text-fill: #ef4444;");
        
        actions.getChildren().addAll(editButton, deleteButton);
        
        header.getChildren().addAll(info, spacer, statusToggle);
        card.getChildren().addAll(header, actions);
        
        return card;
    }
    
    private void updateSummary() {
        try {
            double totalMonthly = subscriptions.stream()
                .mapToDouble(Subscription::getMonthlyCost)
                .sum();
            
            double totalAnnual = subscriptions.stream()
                .mapToDouble(Subscription::getAnnualCost)
                .sum();
            
            totalMonthlyLabel.setText(String.format("$%.2f", totalMonthly));
            totalAnnualLabel.setText(String.format("$%.2f", totalAnnual));
            
            // Calculate percentage of income
            com.argonomix.models.User user = SessionManager.getCurrentUser();
            if (user != null && user.getMonthlyIncome() > 0) {
                double percent = (totalMonthly / user.getMonthlyIncome()) * 100;
                percentOfIncomeLabel.setText(String.format("%.1f%% of monthly income", percent));
            } else {
                percentOfIncomeLabel.setText("N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddSubscription() {
        showSubscriptionModal(null);
    }
    
    private void handleEditSubscription(Subscription subscription) {
        showSubscriptionModal(subscription);
    }
    
    private void handleDeleteSubscription(Subscription subscription) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Subscription");
        confirm.setHeaderText("Are you sure you want to delete this subscription?");
        confirm.setContentText("This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                subscriptionDAO.delete(subscription.getSubscriptionId());
                loadSubscriptions();
            } catch (Exception e) {
                showError("Error deleting subscription: " + e.getMessage());
            }
        }
    }
    
    private void showSubscriptionModal(Subscription subscription) {
        // TODO: Implement subscription modal
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(subscription == null ? "Add Subscription" : "Edit Subscription");
        alert.setHeaderText("Subscription Form");
        alert.setContentText("Subscription modal will be implemented here.");
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


