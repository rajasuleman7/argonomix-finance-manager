package com.argonomix.controllers;

import com.argonomix.database.InvestmentDAO;
import com.argonomix.models.Investment;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class InvestmentController implements Initializable {
    
    @FXML private TableView<Investment> investmentsTable;
    @FXML private TableColumn<Investment, String> nameColumn;
    @FXML private TableColumn<Investment, String> typeColumn;
    @FXML private TableColumn<Investment, Double> investedColumn;
    @FXML private TableColumn<Investment, Double> currentValueColumn;
    @FXML private TableColumn<Investment, Double> returnColumn;
    @FXML private TableColumn<Investment, Double> returnPercentColumn;
    @FXML private Label totalInvestedLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label totalReturnLabel;
    
    private InvestmentDAO investmentDAO;
    private ObservableList<Investment> investments = FXCollections.observableArrayList();
    
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
        
        investmentDAO = new InvestmentDAO();
        setupTable();
        loadInvestments();
    }
    
    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("investmentName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("investmentType"));
        investedColumn.setCellValueFactory(new PropertyValueFactory<>("amountInvested"));
        investedColumn.setCellFactory(column -> new TableCell<Investment, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("$%.2f", amount));
            }
        });
        
        currentValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        currentValueColumn.setCellFactory(column -> new TableCell<Investment, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("$%.2f", value));
            }
        });
        
        returnColumn.setCellValueFactory(cellData -> {
            Investment inv = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(inv.getReturn()).asObject();
        });
        returnColumn.setCellFactory(column -> new TableCell<Investment, Double>() {
            @Override
            protected void updateItem(Double returnValue, boolean empty) {
                super.updateItem(returnValue, empty);
                if (empty || returnValue == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", returnValue));
                    setStyle(returnValue >= 0 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #ef4444;");
                }
            }
        });
        
        returnPercentColumn.setCellValueFactory(cellData -> {
            Investment inv = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(inv.getReturnPercentage()).asObject();
        });
        returnPercentColumn.setCellFactory(column -> new TableCell<Investment, Double>() {
            @Override
            protected void updateItem(Double percent, boolean empty) {
                super.updateItem(percent, empty);
                if (empty || percent == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f%%", percent));
                    setStyle(percent >= 0 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #ef4444;");
                }
            }
        });
        
        investmentsTable.setItems(investments);
    }
    
    private void loadInvestments() {
        try {
            int userId = SessionManager.getCurrentUserId();
            List<Investment> investmentList = investmentDAO.findByUserId(userId);
            investments.setAll(investmentList);
            updateSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateSummary() {
        double totalInvested = investments.stream()
            .mapToDouble(Investment::getAmountInvested)
            .sum();
        
        double totalValue = investments.stream()
            .mapToDouble(Investment::getCurrentValue)
            .sum();
        
        double totalReturn = totalValue - totalInvested;
        
        totalInvestedLabel.setText(String.format("$%.2f", totalInvested));
        totalValueLabel.setText(String.format("$%.2f", totalValue));
        totalReturnLabel.setText(String.format("$%.2f", totalReturn));
    }
    
    @FXML
    private void handleAddInvestment() {
        showInvestmentModal(null);
    }
    
    @FXML
    private void handleEditInvestment() {
        Investment selected = investmentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showInvestmentModal(selected);
        }
    }
    
    @FXML
    private void handleDeleteInvestment() {
        Investment selected = investmentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Investment");
            confirm.setHeaderText("Are you sure you want to delete this investment?");
            confirm.setContentText("This action cannot be undone.");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    investmentDAO.delete(selected.getInvestmentId());
                    loadInvestments();
                } catch (Exception e) {
                    showError("Error deleting investment: " + e.getMessage());
                }
            }
        }
    }
    
    private void showInvestmentModal(Investment investment) {
        // TODO: Implement investment modal
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(investment == null ? "Add Investment" : "Edit Investment");
        alert.setHeaderText("Investment Form");
        alert.setContentText("Investment modal will be implemented here.");
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


