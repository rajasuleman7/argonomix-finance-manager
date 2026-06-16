package com.argonomix.controllers;

import com.argonomix.database.TransactionDAO;
import com.argonomix.models.Transaction;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TransactionController implements Initializable {
    
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> dateRangeFilter;
    @FXML private DatePicker customStartDate;
    @FXML private DatePicker customEndDate;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    
    private TransactionDAO transactionDAO;
    private ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
    private ObservableList<Transaction> filteredTransactions = FXCollections.observableArrayList();
    
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
        
        transactionDAO = new TransactionDAO();
        setupTable();
        setupFilters();
        loadTransactions();
    }
    
    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        dateColumn.setCellFactory(column -> new TableCell<Transaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                }
            }
        });
        
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    String sign = transaction.getTransactionType() == Transaction.TransactionType.INCOME ? "+" : "-";
                    setText(String.format("%s$%.2f", sign, amount));
                    setStyle(transaction.getTransactionType() == Transaction.TransactionType.INCOME ? 
                        "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });
        
        typeColumn.setCellValueFactory(cellData -> {
            Transaction.TransactionType type = cellData.getValue().getTransactionType();
            return new javafx.beans.property.SimpleStringProperty(type.name());
        });
        
        transactionsTable.setItems(filteredTransactions);
    }
    
    private void setupFilters() {
        typeFilter.getItems().addAll("All Types", "Income", "Expense");
        typeFilter.setValue("All Types");
        
        dateRangeFilter.getItems().addAll("All Time", "Today", "Last 7 Days", "This Month", "Last 3 Months", "Custom Range");
        dateRangeFilter.setValue("All Time");
        
        dateRangeFilter.setOnAction(e -> {
            if ("Custom Range".equals(dateRangeFilter.getValue())) {
                customStartDate.setVisible(true);
                customEndDate.setVisible(true);
            } else {
                customStartDate.setVisible(false);
                customEndDate.setVisible(false);
            }
            applyFilters();
        });
        
        customStartDate.setVisible(false);
        customEndDate.setVisible(false);
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        typeFilter.setOnAction(e -> applyFilters());
        categoryFilter.setOnAction(e -> applyFilters());
    }
    
    private void loadTransactions() {
        try {
            int userId = SessionManager.getCurrentUserId();
            List<Transaction> transactions = transactionDAO.findByUserId(userId);
            allTransactions.setAll(transactions);
            
            // Update category filter
            List<String> categories = transactions.stream()
                .map(Transaction::getCategory)
                .filter(cat -> cat != null && !cat.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            categoryFilter.getItems().addAll(categories);
            categoryFilter.setValue("All Categories");
            
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void applyFilters() {
        List<Transaction> filtered = allTransactions.stream()
            .filter(t -> {
                // Search filter
                String search = searchField.getText().toLowerCase();
                if (!search.isEmpty()) {
                    boolean matchesDescription = t.getDescription() != null && 
                        t.getDescription().toLowerCase().contains(search);
                    boolean matchesCategory = t.getCategory() != null && 
                        t.getCategory().toLowerCase().contains(search);
                    if (!matchesDescription && !matchesCategory) {
                        return false;
                    }
                }
                
                // Type filter
                String typeFilterValue = typeFilter.getValue();
                if (typeFilterValue != null && !typeFilterValue.equals("All Types")) {
                    Transaction.TransactionType expectedType = typeFilterValue.equals("Income") ? 
                        Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;
                    if (t.getTransactionType() != expectedType) {
                        return false;
                    }
                }
                
                // Category filter
                String categoryFilterValue = categoryFilter.getValue();
                if (categoryFilterValue != null && !categoryFilterValue.equals("All Categories")) {
                    if (!categoryFilterValue.equals(t.getCategory())) {
                        return false;
                    }
                }
                
                // Date range filter
                String dateRange = dateRangeFilter.getValue();
                if (dateRange != null && !dateRange.equals("All Time")) {
                    LocalDate now = LocalDate.now();
                    LocalDate start = null;
                    LocalDate end = null;
                    
                    switch (dateRange) {
                        case "Today":
                            start = now;
                            end = now;
                            break;
                        case "Last 7 Days":
                            start = now.minusDays(7);
                            end = now;
                            break;
                        case "This Month":
                            start = now.withDayOfMonth(1);
                            end = now.withDayOfMonth(now.lengthOfMonth());
                            break;
                        case "Last 3 Months":
                            start = now.minusMonths(3);
                            end = now;
                            break;
                        case "Custom Range":
                            if (customStartDate.getValue() != null && customEndDate.getValue() != null) {
                                start = customStartDate.getValue();
                                end = customEndDate.getValue();
                            }
                            break;
                    }
                    
                    if (start != null && end != null) {
                        LocalDate transactionDate = t.getTransactionDate();
                        if (transactionDate == null || transactionDate.isBefore(start) || transactionDate.isAfter(end)) {
                            return false;
                        }
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        filteredTransactions.setAll(filtered);
        updateSummary();
    }
    
    private void updateSummary() {
        double totalIncome = filteredTransactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.INCOME)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double totalExpenses = filteredTransactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
    }
    
    @FXML
    private void handleAddTransaction() {
        // Open add transaction modal
        showTransactionModal(null);
    }
    
    @FXML
    private void handleEditTransaction() {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showTransactionModal(selected);
        }
    }
    
    @FXML
    private void handleDeleteTransaction() {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Transaction");
            confirm.setHeaderText("Are you sure you want to delete this transaction?");
            confirm.setContentText("This action cannot be undone.");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    transactionDAO.delete(selected.getTransactionId());
                    loadTransactions();
                } catch (Exception e) {
                    showError("Error deleting transaction: " + e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        Stage stage = (Stage) transactionsTable.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                importCSV(file);
                loadTransactions();
                showSuccess("Transactions imported successfully!");
            } catch (Exception e) {
                showError("Error importing CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV File");
        fileChooser.setInitialFileName("transactions-" + LocalDate.now().toString() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        Stage stage = (Stage) transactionsTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                exportCSV(file);
                showSuccess("Transactions exported successfully!");
            } catch (Exception e) {
                showError("Error exporting CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void importCSV(File file) throws Exception {
        int userId = SessionManager.getCurrentUserId();
        
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> rows = reader.readAll();
            
            if (rows.isEmpty() || rows.size() == 1) {
                return; // Header only or empty
            }
            
            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 4) continue;
                
                try {
                    String dateStr = row[0];
                    String description = row[1];
                    String category = row.length > 2 ? row[2] : "Other";
                    double amount = Double.parseDouble(row[3]);
                    String typeStr = row.length > 4 ? row[4] : "expense";
                    
                    LocalDate date = LocalDate.parse(dateStr);
                    Transaction.TransactionType type = typeStr.equalsIgnoreCase("income") ? 
                        Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;
                    
                    Transaction transaction = new Transaction(userId, amount, category, description, type, date);
                    transactionDAO.save(transaction);
                } catch (Exception e) {
                    // Skip invalid rows
                    System.err.println("Skipping invalid row: " + String.join(",", row));
                }
            }
        }
    }
    
    private void exportCSV(File file) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // Write header
            writer.writeNext(new String[]{"Date", "Description", "Category", "Amount", "Type"});
            
            // Write data
            for (Transaction transaction : filteredTransactions) {
                writer.writeNext(new String[]{
                    transaction.getTransactionDate().toString(),
                    transaction.getDescription(),
                    transaction.getCategory(),
                    String.valueOf(transaction.getAmount()),
                    transaction.getTransactionType().name()
                });
            }
        }
    }
    
    private void showTransactionModal(Transaction transaction) {
        // TODO: Implement transaction modal
        // For now, show a simple alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(transaction == null ? "Add Transaction" : "Edit Transaction");
        alert.setHeaderText("Transaction Form");
        alert.setContentText("Transaction modal will be implemented here.");
        alert.showAndWait();
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


