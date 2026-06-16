package com.argonomix.controllers;

import com.argonomix.database.*;
import com.argonomix.models.*;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class OnboardingController implements Initializable {
    
    @FXML private VBox onboardingContainer;
    @FXML private ProgressIndicator stepIndicator;
    @FXML private Label stepLabel;
    
    private int currentStep = 1;
    private final int totalSteps = 5;
    
    private UserDAO userDAO;
    private BudgetDAO budgetDAO;
    private GoalDAO goalDAO;
    private UserSettingsDAO settingsDAO;
    
    private User currentUser;
    private String selectedTemplate;
    private String goalName;
    private String goalTarget;
    
    private Map<String, List<BudgetTemplate>> budgetTemplates = new HashMap<>();
    
    private class BudgetTemplate {
        String category;
        double limit;
        
        BudgetTemplate(String category, double limit) {
            this.category = category;
            this.limit = limit;
        }
    }
    
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
        
        userDAO = new UserDAO();
        budgetDAO = new BudgetDAO();
        goalDAO = new GoalDAO();
        settingsDAO = new UserSettingsDAO();
        
        currentUser = SessionManager.getCurrentUser();
        
        initializeTemplates();
        showStep(1);
    }
    
    private void initializeTemplates() {
        List<BudgetTemplate> brokeStudent = Arrays.asList(
            new BudgetTemplate("Food", 200),
            new BudgetTemplate("Transportation", 50),
            new BudgetTemplate("Entertainment", 50),
            new BudgetTemplate("Shopping", 100),
            new BudgetTemplate("Bills", 300)
        );
        
        List<BudgetTemplate> partTime = Arrays.asList(
            new BudgetTemplate("Food", 300),
            new BudgetTemplate("Transportation", 100),
            new BudgetTemplate("Entertainment", 150),
            new BudgetTemplate("Shopping", 200),
            new BudgetTemplate("Bills", 500)
        );
        
        List<BudgetTemplate> intern = Arrays.asList(
            new BudgetTemplate("Food", 250),
            new BudgetTemplate("Transportation", 75),
            new BudgetTemplate("Entertainment", 100),
            new BudgetTemplate("Shopping", 150),
            new BudgetTemplate("Bills", 400)
        );
        
        budgetTemplates.put("Broke Student", brokeStudent);
        budgetTemplates.put("Part-Time Worker", partTime);
        budgetTemplates.put("Intern Life", intern);
    }
    
    private void showStep(int step) {
        currentStep = step;
        onboardingContainer.getChildren().clear();
        stepIndicator.setProgress((double) step / totalSteps);
        stepLabel.setText(String.format("Step %d of %d", step, totalSteps));
        
        switch (step) {
            case 1:
                showWelcomeStep();
                break;
            case 2:
                showProfileStep();
                break;
            case 3:
                showBudgetTemplateStep();
                break;
            case 4:
                showGoalStep();
                break;
            case 5:
                showCompletionStep();
                break;
        }
    }
    
    private void showWelcomeStep() {
        VBox content = new VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-padding: 40px;");
        
        Label title = new Label("Welcome to ARGonomix!");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        
        Label description = new Label("Let's get you set up in just a few steps. We'll help you create your profile, set up a budget, and create your first savings goal.");
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #6b7280;");
        
        Button nextButton = new Button("Get Started");
        nextButton.setOnAction(e -> showStep(2));
        nextButton.getStyleClass().add("primary-button");
        
        content.getChildren().addAll(title, description, nextButton);
        onboardingContainer.getChildren().add(content);
    }
    
    private void showProfileStep() {
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 40px;");
        
        Label title = new Label("Profile Setup");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        TextField monthlyIncomeField = new TextField();
        monthlyIncomeField.setPromptText("Monthly Income ($)");
        if (currentUser.getMonthlyIncome() > 0) {
            monthlyIncomeField.setText(String.valueOf(currentUser.getMonthlyIncome()));
        }
        
        TextField hourlyWageField = new TextField();
        hourlyWageField.setPromptText("Hourly Wage ($)");
        if (currentUser.getHourlyWage() > 0) {
            hourlyWageField.setText(String.valueOf(currentUser.getHourlyWage()));
        }
        
        HBox buttons = new HBox(10);
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showStep(1));
        backButton.getStyleClass().add("secondary-button");
        
        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> {
            try {
                if (!monthlyIncomeField.getText().trim().isEmpty()) {
                    currentUser.setMonthlyIncome(Double.parseDouble(monthlyIncomeField.getText().trim()));
                }
                if (!hourlyWageField.getText().trim().isEmpty()) {
                    currentUser.setHourlyWage(Double.parseDouble(hourlyWageField.getText().trim()));
                }
                userDAO.update(currentUser);
                SessionManager.setCurrentUser(currentUser);
                showStep(3);
            } catch (NumberFormatException ex) {
                showError("Invalid number format");
            } catch (Exception ex) {
                showError("Error updating profile: " + ex.getMessage());
            }
        });
        nextButton.getStyleClass().add("primary-button");
        
        buttons.getChildren().addAll(backButton, nextButton);
        content.getChildren().addAll(title, monthlyIncomeField, hourlyWageField, buttons);
        onboardingContainer.getChildren().add(content);
    }
    
    private void showBudgetTemplateStep() {
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 40px;");
        
        Label title = new Label("Choose a Budget Template");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label description = new Label("Select a budget template that matches your lifestyle. You can customize it later.");
        description.setWrapText(true);
        
        VBox templatesContainer = new VBox(10);
        for (String templateName : budgetTemplates.keySet()) {
            Button templateButton = new Button(templateName);
            templateButton.setPrefWidth(Double.MAX_VALUE);
            templateButton.setOnAction(e -> {
                selectedTemplate = templateName;
                templateButton.setStyle("-fx-background-color: #9333ea; -fx-text-fill: white;");
            });
            templateButton.getStyleClass().add("secondary-button");
            templatesContainer.getChildren().add(templateButton);
        }
        
        HBox buttons = new HBox(10);
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showStep(2));
        backButton.getStyleClass().add("secondary-button");
        
        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> {
            if (selectedTemplate != null) {
                try {
                    int userId = SessionManager.getCurrentUserId();
                    List<BudgetTemplate> templates = budgetTemplates.get(selectedTemplate);
                    
                    for (BudgetTemplate template : templates) {
                        Budget budget = new Budget(userId, template.category, template.limit, "monthly");
                        budgetDAO.save(budget);
                    }
                    
                    showStep(4);
                } catch (Exception ex) {
                    showError("Error creating budgets: " + ex.getMessage());
                }
            } else {
                showError("Please select a budget template");
            }
        });
        nextButton.getStyleClass().add("primary-button");
        
        buttons.getChildren().addAll(backButton, nextButton);
        content.getChildren().addAll(title, description, templatesContainer, buttons);
        onboardingContainer.getChildren().add(content);
    }
    
    private void showGoalStep() {
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 40px;");
        
        Label title = new Label("Set Your First Goal");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label description = new Label("Create a savings goal to get started. You can add more goals later.");
        description.setWrapText(true);
        
        TextField goalNameField = new TextField();
        goalNameField.setPromptText("Goal Name");
        
        TextField goalTargetField = new TextField();
        goalTargetField.setPromptText("Target Amount ($)");
        
        HBox buttons = new HBox(10);
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showStep(3));
        backButton.getStyleClass().add("secondary-button");
        
        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> {
            goalName = goalNameField.getText().trim();
            goalTarget = goalTargetField.getText().trim();
            
            if (goalName.isEmpty() || goalTarget.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }
            
            try {
                double target = Double.parseDouble(goalTarget);
                if (target <= 0) {
                    showError("Target amount must be greater than 0");
                    return;
                }
                
                int userId = SessionManager.getCurrentUserId();
                Goal goal = new Goal();
                goal.setUserId(userId);
                goal.setGoalName(goalName);
                goal.setTargetAmount(target);
                goal.setDeadline(LocalDate.now().plusYears(1));
                goal.setGoalIcon("🎯");
                
                goalDAO.save(goal);
                showStep(5);
            } catch (NumberFormatException ex) {
                showError("Invalid target amount");
            } catch (Exception ex) {
                showError("Error creating goal: " + ex.getMessage());
            }
        });
        nextButton.getStyleClass().add("primary-button");
        
        buttons.getChildren().addAll(backButton, nextButton);
        content.getChildren().addAll(title, description, goalNameField, goalTargetField, buttons);
        onboardingContainer.getChildren().add(content);
    }
    
    private void showCompletionStep() {
        VBox content = new VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-padding: 40px;");
        
        Label emoji = new Label("🎉");
        emoji.setStyle("-fx-font-size: 64px;");
        
        Label title = new Label("You're All Set!");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        
        Label description = new Label("You're ready to start managing your finances. Explore the dashboard to see your financial overview and start tracking your expenses.");
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #6b7280;");
        
        Button finishButton = new Button("Get Started");
        finishButton.setOnAction(e -> {
            try {
                int userId = SessionManager.getCurrentUserId();
                UserSettings settings = settingsDAO.getOrCreate(userId);
                settings.setOnboardingComplete(true);
                settingsDAO.update(settings);
                
                NavigationUtil.switchToScene("/fxml/Dashboard.fxml");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        finishButton.getStyleClass().add("primary-button");
        
        content.getChildren().addAll(emoji, title, description, finishButton);
        onboardingContainer.getChildren().add(content);
    }
    
    @FXML
    private void handleSkip() {
        try {
            int userId = SessionManager.getCurrentUserId();
            UserSettings settings = settingsDAO.getOrCreate(userId);
            settings.setOnboardingComplete(true);
            settingsDAO.update(settings);
            
            NavigationUtil.switchToScene("/fxml/Dashboard.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


