package com.argonomix.controllers;

import com.argonomix.database.TransactionDAO;
import com.argonomix.models.Transaction;
import com.argonomix.utils.FinancialCalculations;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class BuyCalculatorController implements Initializable {
    
    @FXML private TextField itemPriceField;
    @FXML private Button calculateButton;
    @FXML private VBox resultContainer;
    @FXML private Label recommendationLabel;
    @FXML private Label hoursOfWorkLabel;
    @FXML private Label daysToDelayLabel;
    @FXML private Label percentOfIncomeLabel;
    @FXML private Label reasoningLabel;
    
    private TransactionDAO transactionDAO;
    private FinancialCalculations.BuyDecisionResult currentResult;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        transactionDAO = new TransactionDAO();
        resultContainer.setVisible(false);
    }
    
    @FXML
    private void handleCalculate() {
        try {
            String priceText = itemPriceField.getText().trim();
            if (priceText.isEmpty()) {
                showError("Please enter an item price");
                return;
            }
            
            double itemPrice = Double.parseDouble(priceText);
            if (itemPrice <= 0) {
                showError("Price must be greater than 0");
                return;
            }
            
            int userId = SessionManager.getCurrentUserId();
            currentResult = FinancialCalculations.calculateBuyDecision(userId, itemPrice);
            
            displayResult();
        } catch (NumberFormatException e) {
            showError("Invalid price format");
        } catch (Exception e) {
            showError("Error calculating: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayResult() {
        if (currentResult == null) return;
        
        resultContainer.setVisible(true);
        
        hoursOfWorkLabel.setText(String.format("%.1f hrs", currentResult.getHoursOfWork()));
        daysToDelayLabel.setText(String.format("%.1f days", currentResult.getDaysToDelayGoal()));
        percentOfIncomeLabel.setText(String.format("%.1f%%", currentResult.getPercentOfIncome()));
        
        String recommendation = currentResult.getRecommendation();
        String recommendationText = "";
        String style = "";
        
        if ("affordable".equals(recommendation)) {
            recommendationText = "✓ Affordable";
            style = "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 8px; -fx-background-radius: 8px;";
        } else if ("think-twice".equals(recommendation)) {
            recommendationText = "⚠ Think Twice";
            style = "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 8px; -fx-background-radius: 8px;";
        } else {
            recommendationText = "✗ Not Recommended";
            style = "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 8px; -fx-background-radius: 8px;";
        }
        
        recommendationLabel.setText(recommendationText);
        recommendationLabel.setStyle(style);
        reasoningLabel.setText(currentResult.getReasoning());
    }
    
    @FXML
    private void handleBought() {
        if (currentResult == null || itemPriceField.getText().trim().isEmpty()) {
            return;
        }
        
        try {
            double price = Double.parseDouble(itemPriceField.getText().trim());
            int userId = SessionManager.getCurrentUserId();
            
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setAmount(price);
            transaction.setCategory("Shopping");
            transaction.setDescription("Purchase");
            transaction.setTransactionDate(LocalDate.now());
            transaction.setTransactionType(Transaction.TransactionType.EXPENSE);
            
            transactionDAO.save(transaction);
            
            itemPriceField.clear();
            resultContainer.setVisible(false);
            currentResult = null;
            
            showSuccess("Transaction added successfully!");
        } catch (Exception e) {
            showError("Error adding transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSaved() {
        itemPriceField.clear();
        resultContainer.setVisible(false);
        currentResult = null;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


