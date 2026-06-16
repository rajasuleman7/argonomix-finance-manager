package com.argonomix.controllers;

import com.argonomix.models.User;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LayoutController implements Initializable {
    
    @FXML private BorderPane mainContainer;
    @FXML private VBox sidebar;
    @FXML private Label userNameLabel;
    @FXML private Button dashboardButton;
    @FXML private Button transactionsButton;
    @FXML private Button budgetsButton;
    @FXML private Button expensesButton;
    @FXML private Button goalsButton;
    @FXML private Button investmentsButton;
    @FXML private Button billSplitButton;
    @FXML private Button challengesButton;
    @FXML private Button healthScoreButton;
    @FXML private Button reportsButton;
    @FXML private Button subscriptionsButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;
    @FXML private Button addTransactionButton;
    
    private User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName() != null ? currentUser.getName() : "User");
        }
        
        setupNavigation();
    }
    
    private void setupNavigation() {
        dashboardButton.setOnAction(e -> navigateTo("/fxml/Dashboard.fxml"));
        transactionsButton.setOnAction(e -> navigateTo("/fxml/Transactions.fxml"));
        budgetsButton.setOnAction(e -> navigateTo("/fxml/Budgets.fxml"));
        expensesButton.setOnAction(e -> navigateTo("/fxml/Expenses.fxml"));
        goalsButton.setOnAction(e -> navigateTo("/fxml/Goals.fxml"));
        investmentsButton.setOnAction(e -> navigateTo("/fxml/Investments.fxml"));
        billSplitButton.setOnAction(e -> navigateTo("/fxml/BillSplit.fxml"));
        challengesButton.setOnAction(e -> navigateTo("/fxml/Challenges.fxml"));
        healthScoreButton.setOnAction(e -> navigateTo("/fxml/HealthScore.fxml"));
        reportsButton.setOnAction(e -> navigateTo("/fxml/Reports.fxml"));
        subscriptionsButton.setOnAction(e -> navigateTo("/fxml/Subscriptions.fxml"));
        settingsButton.setOnAction(e -> navigateTo("/fxml/Settings.fxml"));
        logoutButton.setOnAction(e -> handleLogout());
        addTransactionButton.setOnAction(e -> navigateTo("/fxml/Transactions.fxml"));
    }
    
    private void navigateTo(String fxmlFile) {
        try {
            NavigationUtil.switchToScene(fxmlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        try {
            NavigationUtil.switchToScene("/fxml/Login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setContent(Parent content) {
        mainContainer.setCenter(content);
    }
}


