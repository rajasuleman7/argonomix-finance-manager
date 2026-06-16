package com.argonomix.controllers;

import com.argonomix.database.UserDAO;
import com.argonomix.models.User;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SignupController implements Initializable {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField monthlyIncomeField;
    @FXML private TextField hourlyWageField;
    @FXML private Button signupButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;
    
    private UserDAO userDAO;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        errorLabel.setVisible(false);
        passwordStrengthBar.setVisible(false);
        passwordStrengthLabel.setVisible(false);
        
        // Password strength checker
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkPasswordStrength(newVal);
        });
    }
    
    @FXML
    private void handleSignup() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String monthlyIncomeStr = monthlyIncomeField.getText().trim();
        String hourlyWageStr = hourlyWageField.getText().trim();
        
        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all required fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }
        
        try {
            if (userDAO.emailExists(email)) {
                showError("Email already registered");
                return;
            }
            
            // Create user
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(UserDAO.hashPassword(password));
            
            if (!monthlyIncomeStr.isEmpty()) {
                try {
                    user.setMonthlyIncome(Double.parseDouble(monthlyIncomeStr));
                } catch (NumberFormatException e) {
                    showError("Invalid monthly income");
                    return;
                }
            }
            
            if (!hourlyWageStr.isEmpty()) {
                try {
                    user.setHourlyWage(Double.parseDouble(hourlyWageStr));
                } catch (NumberFormatException e) {
                    showError("Invalid hourly wage");
                    return;
                }
            }
            
            if (userDAO.save(user)) {
                SessionManager.setCurrentUser(user);
                NavigationUtil.switchToScene("/fxml/Dashboard.fxml");
            } else {
                showError("Failed to create account. Please try again.");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLoginLink() {
        try {
            NavigationUtil.switchToScene("/fxml/Login.fxml");
        } catch (Exception e) {
            showError("Error loading login page: " + e.getMessage());
        }
    }
    
    private void checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setVisible(false);
            passwordStrengthLabel.setVisible(false);
            return;
        }
        
        passwordStrengthBar.setVisible(true);
        passwordStrengthLabel.setVisible(true);
        
        int strength = calculatePasswordStrength(password);
        double progress = strength / 100.0;
        passwordStrengthBar.setProgress(progress);
        
        if (strength < 30) {
            passwordStrengthLabel.setText("Weak");
            passwordStrengthLabel.setStyle("-fx-text-fill: #ef4444;");
        } else if (strength < 60) {
            passwordStrengthLabel.setText("Fair");
            passwordStrengthLabel.setStyle("-fx-text-fill: #f59e0b;");
        } else if (strength < 80) {
            passwordStrengthLabel.setText("Good");
            passwordStrengthLabel.setStyle("-fx-text-fill: #3b82f6;");
        } else {
            passwordStrengthLabel.setText("Strong");
            passwordStrengthLabel.setStyle("-fx-text-fill: #10b981;");
        }
    }
    
    private int calculatePasswordStrength(String password) {
        int strength = 0;
        
        if (password.length() >= 8) strength += 20;
        if (password.length() >= 12) strength += 10;
        if (password.matches(".*[a-z].*")) strength += 10;
        if (password.matches(".*[A-Z].*")) strength += 10;
        if (password.matches(".*[0-9].*")) strength += 10;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength += 20;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*") && 
            password.matches(".*[0-9].*") && password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            strength += 20;
        }
        
        return Math.min(strength, 100);
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}

