package com.argonomix.controllers;

import com.argonomix.utils.FinancialCalculations;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ExpenseController implements Initializable {
    
    @FXML private TabPane expenseTabs;
    @FXML private Tab overviewTab;
    @FXML private Tab categoryTab;
    @FXML private Tab trendsTab;
    @FXML private Tab insightsTab;
    @FXML private ComboBox<String> dateRangeCombo;
    @FXML private PieChart categoryPieChart;
    @FXML private LineChart<String, Number> spendingTrendChart;
    @FXML private VBox categorySpendingContainer;
    @FXML private VBox insightsContainer;
    
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
        
        dateRangeCombo.getItems().addAll("This Week", "This Month", "Last 3 Months", "Last 6 Months");
        dateRangeCombo.setValue("This Month");
        dateRangeCombo.setOnAction(e -> loadExpenseData());
        
        loadExpenseData();
    }
    
    private void loadExpenseData() {
        try {
            int userId = SessionManager.getCurrentUserId();
            String dateRange = dateRangeCombo.getValue();
            
            String rangeKey = dateRange.equals("This Week") ? "week" :
                             dateRange.equals("This Month") ? "month" :
                             dateRange.equals("Last 3 Months") ? "3months" : "6months";
            
            loadCategorySpending(userId, rangeKey);
            loadSpendingTrend(userId, rangeKey);
            loadCategoryInsights(userId, rangeKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadCategorySpending(int userId, String dateRange) throws Exception {
        List<FinancialCalculations.CategorySpending> spending = 
            FinancialCalculations.calculateSpendingByCategory(userId, dateRange);
        
        categoryPieChart.getData().clear();
        categorySpendingContainer.getChildren().clear();
        
        for (FinancialCalculations.CategorySpending cat : spending) {
            PieChart.Data slice = new PieChart.Data(cat.getCategory(), cat.getAmount());
            categoryPieChart.getData().add(slice);
            
            VBox categoryCard = createCategoryCard(cat);
            categorySpendingContainer.getChildren().add(categoryCard);
        }
    }
    
    private VBox createCategoryCard(FinancialCalculations.CategorySpending cat) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-padding: 15px;");
        
        Label categoryLabel = new Label(cat.getCategory());
        categoryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label amountLabel = new Label(String.format("$%.2f", cat.getAmount()));
        amountLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #9333ea;");
        
        card.getChildren().addAll(categoryLabel, amountLabel);
        return card;
    }
    
    private void loadSpendingTrend(int userId, String dateRange) throws Exception {
        int days = dateRange.equals("week") ? 7 : 
                  dateRange.equals("month") ? 30 : 
                  dateRange.equals("3months") ? 90 : 180;
        
        List<FinancialCalculations.SpendingTrendData> trend = 
            FinancialCalculations.calculateSpendingTrend(userId, days);
        
        spendingTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Spending");
        
        for (FinancialCalculations.SpendingTrendData data : trend) {
            series.getData().add(new XYChart.Data<>(data.getDate(), data.getAmount()));
        }
        
        spendingTrendChart.getData().add(series);
    }
    
    private void loadCategoryInsights(int userId, String dateRange) throws Exception {
        insightsContainer.getChildren().clear();
        
        List<FinancialCalculations.CategorySpending> spending = 
            FinancialCalculations.calculateSpendingByCategory(userId, dateRange);
        
        for (FinancialCalculations.CategorySpending cat : spending) {
            VBox insightCard = createInsightCard(cat, dateRange);
            insightsContainer.getChildren().add(insightCard);
        }
    }
    
    private VBox createInsightCard(FinancialCalculations.CategorySpending cat, String dateRange) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 20px;");
        
        Label categoryLabel = new Label(cat.getCategory());
        categoryLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        double monthlyAvg = cat.getAmount() / (dateRange.equals("week") ? 0.25 : 
                                                dateRange.equals("month") ? 1 : 
                                                dateRange.equals("3months") ? 3 : 6);
        
        Label avgLabel = new Label(String.format("Average Monthly Spending: $%.2f", monthlyAvg));
        avgLabel.setStyle("-fx-font-size: 14px;");
        
        Label recommendation = new Label(String.format(
            "Recommendation: You typically spend $%.2f per month on %s. Your spending is consistent with previous periods.",
            monthlyAvg, cat.getCategory().toLowerCase()));
        recommendation.setWrapText(true);
        recommendation.setStyle("-fx-padding: 10px; -fx-background-color: #dbeafe; -fx-background-radius: 8px;");
        
        card.getChildren().addAll(categoryLabel, avgLabel, recommendation);
        return card;
    }
}


