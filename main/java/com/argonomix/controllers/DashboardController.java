package com.argonomix.controllers;

import com.argonomix.database.*;
import com.argonomix.models.*;
import com.argonomix.utils.*;
import com.argonomix.utils.NotificationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label monthlySpendingLabel;
    @FXML private Label savingsRateLabel;
    @FXML private Label healthScoreLabel;
    @FXML private LineChart<String, Number> spendingTrendChart;
    @FXML private TableView<Transaction> recentTransactionsTable;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, String> transactionDescriptionColumn;
    @FXML private TableColumn<Transaction, String> transactionCategoryColumn;
    @FXML private TableColumn<Transaction, Double> transactionAmountColumn;
    @FXML private VBox budgetProgressContainer;
    @FXML private VBox goalsContainer;
    @FXML private VBox upcomingBillsContainer;
    
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;
    private GoalDAO goalDAO;
    private User currentUser;
    
    private ObservableList<Transaction> recentTransactions = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            try {
                NavigationUtil.switchToScene("/fxml/Login.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        userDAO = new UserDAO();
        transactionDAO = new TransactionDAO();
        budgetDAO = new BudgetDAO();
        goalDAO = new GoalDAO();
        
        setupTable();
        refreshData();
        
        // Auto-refresh every 2 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        // Check for notifications
        try {
            NotificationService.checkAndAddNotifications(SessionManager.getCurrentUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupTable() {
        transactionDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTransactionDate();
            return new javafx.beans.property.SimpleStringProperty(
                date != null ? date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : ""
            );
        });
        transactionDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        transactionCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionAmountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
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
                        "-fx-text-fill: #10b981;" : "-fx-text-fill: #ef4444;");
                }
            }
        });
        
        recentTransactionsTable.setItems(recentTransactions);
    }
    
    private void refreshData() {
        try {
            int userId = SessionManager.getCurrentUserId();
            
            // Update welcome message
            welcomeLabel.setText("Welcome back, " + (currentUser.getName() != null ? currentUser.getName() : "User") + "! 👋");
            
            // Calculate stats
            double totalBalance = FinancialCalculations.calculateTotalBalance(userId);
            double monthlySpending = FinancialCalculations.calculateThisMonthSpending(userId);
            double savingsRate = FinancialCalculations.calculateSavingsRate(userId);
            FinancialCalculations.HealthScoreResult healthScore = FinancialCalculations.calculateFinancialHealthScore(userId);
            
            // Update stat labels
            totalBalanceLabel.setText(String.format("$%.2f", totalBalance));
            monthlySpendingLabel.setText(String.format("$%.2f", monthlySpending));
            savingsRateLabel.setText(String.format("%.1f%%", savingsRate));
            healthScoreLabel.setText(String.format("%d/100", healthScore.getScore()));
            
            // Update spending trend chart
            updateSpendingTrendChart(userId);
            
            // Update recent transactions
            updateRecentTransactions(userId);
            
            // Update budget progress
            updateBudgetProgress(userId);
            
            // Update goals
            updateGoals(userId);
            
            // Update upcoming bills
            updateUpcomingBills(userId);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateSpendingTrendChart(int userId) throws Exception {
        List<FinancialCalculations.SpendingTrendData> trendData = 
            FinancialCalculations.calculateSpendingTrend(userId, 7);
        
        spendingTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Spending");
        
        for (FinancialCalculations.SpendingTrendData data : trendData) {
            series.getData().add(new XYChart.Data<>(data.getDate(), data.getAmount()));
        }
        
        spendingTrendChart.getData().add(series);
    }
    
    private void updateRecentTransactions(int userId) throws Exception {
        List<Transaction> transactions = transactionDAO.findByUserId(userId);
        List<Transaction> recent = transactions.stream()
            .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
            .limit(5)
            .collect(Collectors.toList());
        
        recentTransactions.setAll(recent);
    }
    
    private void updateBudgetProgress(int userId) throws Exception {
        budgetProgressContainer.getChildren().clear();
        
        List<Budget> budgets = budgetDAO.findByUserId(userId);
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        List<Transaction> transactions = transactionDAO.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        for (Budget budget : budgets) {
            double spent = transactions.stream()
                .filter(t -> t.getCategory() != null && 
                           t.getCategory().equals(budget.getCategory()) &&
                           t.getTransactionType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            double percentage = budget.getAmountLimit() > 0 ? 
                (spent / budget.getAmountLimit()) * 100 : 0;
            
            VBox budgetCard = createBudgetCard(budget, spent, percentage);
            budgetProgressContainer.getChildren().add(budgetCard);
        }
        
        if (budgets.isEmpty()) {
            Label noBudgetsLabel = new Label("No budgets set. Create your first budget!");
            noBudgetsLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
            budgetProgressContainer.getChildren().add(noBudgetsLabel);
        }
    }
    
    private VBox createBudgetCard(Budget budget, double spent, double percentage) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        HBox header = new HBox();
        header.setSpacing(10);
        Label categoryLabel = new Label(budget.getCategory());
        categoryLabel.setStyle("-fx-font-weight: bold;");
        Label amountLabel = new Label(String.format("$%.2f / $%.2f", spent, budget.getAmountLimit()));
        amountLabel.setStyle("-fx-text-fill: #6b7280;");
        header.getChildren().addAll(categoryLabel, new Label("•"), amountLabel);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(Math.min(percentage / 100.0, 1.0));
        progressBar.setPrefWidth(Double.MAX_VALUE);
        
        if (percentage > 100) {
            progressBar.setStyle("-fx-accent: #ef4444;");
        } else if (percentage > 80) {
            progressBar.setStyle("-fx-accent: #f59e0b;");
        } else {
            progressBar.setStyle("-fx-accent: #10b981;");
        }
        
        card.getChildren().addAll(header, progressBar);
        return card;
    }
    
    private void updateGoals(int userId) throws Exception {
        goalsContainer.getChildren().clear();
        
        List<Goal> goals = goalDAO.findActiveByUserId(userId);
        
        for (Goal goal : goals.stream().limit(3).collect(Collectors.toList())) {
            VBox goalCard = createGoalCard(goal);
            goalsContainer.getChildren().add(goalCard);
        }
        
        if (goals.isEmpty()) {
            Label noGoalsLabel = new Label("No goals yet. Create your first savings goal!");
            noGoalsLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
            goalsContainer.getChildren().add(noGoalsLabel);
        }
    }
    
    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        HBox header = new HBox(10);
        Label iconLabel = new Label(goal.getGoalIcon() != null ? goal.getGoalIcon() : "🎯");
        iconLabel.setStyle("-fx-font-size: 20px;");
        Label nameLabel = new Label(goal.getGoalName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        header.getChildren().addAll(iconLabel, nameLabel);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(goal.getProgressPercentage() / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #9333ea;");
        
        Label amountLabel = new Label(String.format("$%.2f / $%.2f", 
            goal.getCurrentAmount(), goal.getTargetAmount()));
        amountLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        
        card.getChildren().addAll(header, progressBar, amountLabel);
        return card;
    }
    
    @FXML
    private void handleViewAllTransactions() {
        try {
            NavigationUtil.switchToScene("/fxml/Transactions.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleManageBudgets() {
        try {
            NavigationUtil.switchToScene("/fxml/Budgets.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewAllGoals() {
        try {
            NavigationUtil.switchToScene("/fxml/Goals.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateUpcomingBills(int userId) throws Exception {
        upcomingBillsContainer.getChildren().clear();
        
        List<Transaction> transactions = transactionDAO.findByUserId(userId);
        List<Transaction> bills = transactions.stream()
            .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE &&
                        t.getCategory() != null && t.getCategory().equals("Bills"))
            .sorted((a, b) -> a.getTransactionDate().compareTo(b.getTransactionDate()))
            .limit(3)
            .collect(Collectors.toList());
        
        for (Transaction bill : bills) {
            VBox billCard = createBillCard(bill);
            upcomingBillsContainer.getChildren().add(billCard);
        }
        
        if (bills.isEmpty()) {
            Label noBillsLabel = new Label("No upcoming bills");
            noBillsLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
            upcomingBillsContainer.getChildren().add(noBillsLabel);
        }
    }
    
    private VBox createBillCard(Transaction bill) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        Label descriptionLabel = new Label(bill.getDescription());
        descriptionLabel.setStyle("-fx-font-weight: bold;");
        
        Label dateLabel = new Label(bill.getTransactionDate().format(DateTimeFormatter.ofPattern("MMM dd")));
        dateLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        
        Label amountLabel = new Label(String.format("$%.2f", bill.getAmount()));
        amountLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        
        card.getChildren().addAll(descriptionLabel, dateLabel, amountLabel);
        return card;
    }
}

