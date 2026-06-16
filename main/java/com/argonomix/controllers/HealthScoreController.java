package com.argonomix.controllers;

import com.argonomix.utils.FinancialCalculations;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class HealthScoreController implements Initializable {
    
    @FXML private Label healthScoreLabel;
    @FXML private ProgressBar healthScoreBar;
    @FXML private Label savingsRateScoreLabel;
    @FXML private Label budgetAdherenceScoreLabel;
    @FXML private Label emergencyFundScoreLabel;
    @FXML private Label debtToIncomeScoreLabel;
    @FXML private VBox recommendationsContainer;
    
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
        
        loadHealthScore();
    }
    
    private void loadHealthScore() {
        try {
            int userId = SessionManager.getCurrentUserId();
            FinancialCalculations.HealthScoreResult result = 
                FinancialCalculations.calculateFinancialHealthScore(userId);
            
            int score = result.getScore();
            healthScoreLabel.setText(String.format("%d/100", score));
            healthScoreBar.setProgress(score / 100.0);
            
            Map<String, Integer> breakdown = result.getBreakdown();
            savingsRateScoreLabel.setText(String.format("%d/30", breakdown.getOrDefault("savingsRate", 0)));
            budgetAdherenceScoreLabel.setText(String.format("%d/25", breakdown.getOrDefault("budgetAdherence", 0)));
            emergencyFundScoreLabel.setText(String.format("%d/25", breakdown.getOrDefault("emergencyFund", 0)));
            debtToIncomeScoreLabel.setText(String.format("%d/20", breakdown.getOrDefault("debtToIncome", 0)));
            
            updateRecommendations(score, breakdown);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateRecommendations(int score, Map<String, Integer> breakdown) {
        recommendationsContainer.getChildren().clear();
        
        if (score < 50) {
            addRecommendation("⚠️ Your financial health needs improvement. Focus on building savings and sticking to budgets.");
        } else if (score < 70) {
            addRecommendation("👍 You're on the right track! Continue building your emergency fund and maintaining good spending habits.");
        } else if (score < 85) {
            addRecommendation("✨ Great job! Your finances are in good shape. Keep up the excellent work!");
        } else {
            addRecommendation("🎉 Excellent! Your financial health is outstanding. You're doing everything right!");
        }
        
        if (breakdown.getOrDefault("savingsRate", 0) < 20) {
            addRecommendation("💡 Try to save at least 20% of your income each month.");
        }
        
        if (breakdown.getOrDefault("budgetAdherence", 0) < 20) {
            addRecommendation("💡 Review your budgets and try to stay within your limits.");
        }
        
        if (breakdown.getOrDefault("emergencyFund", 0) < 20) {
            addRecommendation("💡 Build an emergency fund that covers 3-6 months of expenses.");
        }
    }
    
    private void addRecommendation(String text) {
        Label recommendation = new Label(text);
        recommendation.setWrapText(true);
        recommendation.setStyle("-fx-padding: 10px; -fx-background-color: #f3f4f6; -fx-background-radius: 8px;");
        recommendationsContainer.getChildren().add(recommendation);
    }
}


