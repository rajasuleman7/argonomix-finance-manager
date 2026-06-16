package com.argonomix.controllers;

import com.argonomix.database.BudgetDAO;
import com.argonomix.database.TransactionDAO;
import com.argonomix.models.Budget;
import com.argonomix.models.Transaction;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BudgetController implements Initializable {
    
    @FXML private VBox budgetsContainer;
    @FXML private BarChart<String, Number> budgetChart;
    @FXML private Label overspendingAlert;
    
    private BudgetDAO budgetDAO;
    private TransactionDAO transactionDAO;
    private ObservableList<Budget> budgets = FXCollections.observableArrayList();
    
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
        
        budgetDAO = new BudgetDAO();
        transactionDAO = new TransactionDAO();
        setupChart();
        loadBudgets();
    }
    
    private void setupChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        budgetChart.setTitle("Budget vs Actual Spending");
        budgetChart.setLegendVisible(true);
    }
    
    private void loadBudgets() {
        try {
            int userId = SessionManager.getCurrentUserId();
            List<Budget> budgetList = budgetDAO.findByUserId(userId);
            budgets.setAll(budgetList);
            updateBudgetDisplay();
            updateChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateBudgetDisplay() {
        budgetsContainer.getChildren().clear();
        
        try {
            int userId = SessionManager.getCurrentUserId();
            LocalDate now = LocalDate.now();
            LocalDate monthStart = now.withDayOfMonth(1);
            LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
            
            List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
            
            boolean hasOverspending = false;
            
            for (Budget budget : budgets) {
                double spent = transactions.stream()
                    .filter(t -> t.getCategory() != null && 
                               t.getCategory().equals(budget.getCategory()) &&
                               t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
                
                double percentage = budget.getAmountLimit() > 0 ? 
                    (spent / budget.getAmountLimit()) * 100 : 0;
                
                if (percentage > 100) {
                    hasOverspending = true;
                }
                
                VBox budgetCard = createBudgetCard(budget, spent, percentage);
                budgetsContainer.getChildren().add(budgetCard);
            }
            
            if (hasOverspending) {
                overspendingAlert.setVisible(true);
            } else {
                overspendingAlert.setVisible(false);
            }
            
            if (budgets.isEmpty()) {
                Label noBudgetsLabel = new Label("No budgets created yet. Create one or apply a template!");
                noBudgetsLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
                budgetsContainer.getChildren().add(noBudgetsLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private VBox createBudgetCard(Budget budget, double spent, double percentage) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 20px;");
        
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label categoryLabel = new Label(budget.getCategory());
        categoryLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label amountLabel = new Label(String.format("$%.2f / $%.2f", spent, budget.getAmountLimit()));
        amountLabel.setStyle("-fx-text-fill: #6b7280;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label percentageLabel = new Label(String.format("%.1f%%", percentage));
        if (percentage > 100) {
            percentageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else if (percentage > 80) {
            percentageLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        } else {
            percentageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
        
        header.getChildren().addAll(categoryLabel, spacer, amountLabel, percentageLabel);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(Math.min(percentage / 100.0, 1.0));
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        
        if (percentage > 100) {
            progressBar.setStyle("-fx-accent: #ef4444;");
        } else if (percentage > 80) {
            progressBar.setStyle("-fx-accent: #f59e0b;");
        } else {
            progressBar.setStyle("-fx-accent: #10b981;");
        }
        
        HBox actions = new HBox(10);
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> handleEditBudget(budget));
        editButton.setStyle("-fx-text-fill: #9333ea;");
        
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> handleDeleteBudget(budget));
        deleteButton.setStyle("-fx-text-fill: #ef4444;");
        
        actions.getChildren().addAll(editButton, deleteButton);
        
        card.getChildren().addAll(header, progressBar, actions);
        return card;
    }
    
    private void updateChart() {
        budgetChart.getData().clear();
        
        try {
            int userId = SessionManager.getCurrentUserId();
            LocalDate now = LocalDate.now();
            LocalDate monthStart = now.withDayOfMonth(1);
            LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
            
            List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
            
            XYChart.Series<String, Number> budgetedSeries = new XYChart.Series<>();
            budgetedSeries.setName("Budgeted");
            
            XYChart.Series<String, Number> spentSeries = new XYChart.Series<>();
            spentSeries.setName("Spent");
            
            for (Budget budget : budgets) {
                double spent = transactions.stream()
                    .filter(t -> t.getCategory() != null && 
                               t.getCategory().equals(budget.getCategory()) &&
                               t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
                
                budgetedSeries.getData().add(new XYChart.Data<>(budget.getCategory(), budget.getAmountLimit()));
                spentSeries.getData().add(new XYChart.Data<>(budget.getCategory(), spent));
            }
            
            budgetChart.getData().addAll(budgetedSeries, spentSeries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCreateBudget() {
        showBudgetModal(null);
    }
    
    @FXML
    private void handleApplyTemplate() {
        // TODO: Implement budget templates
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Budget Templates");
        alert.setHeaderText("Budget Templates");
        alert.setContentText("Budget templates feature will be implemented here.");
        alert.showAndWait();
    }
    
    private void handleEditBudget(Budget budget) {
        showBudgetModal(budget);
    }
    
    private void handleDeleteBudget(Budget budget) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Budget");
        confirm.setHeaderText("Are you sure you want to delete this budget?");
        confirm.setContentText("This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                budgetDAO.delete(budget.getBudgetId());
                loadBudgets();
            } catch (Exception e) {
                showError("Error deleting budget: " + e.getMessage());
            }
        }
    }
    
    private void showBudgetModal(Budget budget) {
        // TODO: Implement budget modal
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(budget == null ? "Create Budget" : "Edit Budget");
        alert.setHeaderText("Budget Form");
        alert.setContentText("Budget modal will be implemented here.");
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


