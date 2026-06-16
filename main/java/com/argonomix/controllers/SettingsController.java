package com.argonomix.controllers;

import com.argonomix.database.UserDAO;
import com.argonomix.database.UserSettingsDAO;
import com.argonomix.models.User;
import com.argonomix.models.UserSettings;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField monthlyIncomeField;
    @FXML private TextField hourlyWageField;
    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private CheckBox notificationsCheckbox;
    @FXML private Button saveButton;
    
    private UserDAO userDAO;
    private UserSettingsDAO settingsDAO;
    private User currentUser;
    private UserSettings settings;
    
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
        settingsDAO = new UserSettingsDAO();
        currentUser = SessionManager.getCurrentUser();
        
        themeCombo.getItems().addAll("light", "dark");
        currencyCombo.getItems().addAll("USD", "EUR", "GBP", "CAD", "AUD");
        
        loadSettings();
    }
    
    private void loadSettings() {
        try {
            int userId = SessionManager.getCurrentUserId();
            
            // Load user profile
            currentUser = userDAO.findById(userId);
            if (currentUser != null) {
                nameField.setText(currentUser.getName());
                emailField.setText(currentUser.getEmail());
                monthlyIncomeField.setText(currentUser.getMonthlyIncome() > 0 ? 
                    String.valueOf(currentUser.getMonthlyIncome()) : "");
                hourlyWageField.setText(currentUser.getHourlyWage() > 0 ? 
                    String.valueOf(currentUser.getHourlyWage()) : "");
            }
            
            // Load settings
            settings = settingsDAO.getOrCreate(userId);
            themeCombo.setValue(settings.getTheme());
            currencyCombo.setValue(settings.getCurrency());
            notificationsCheckbox.setSelected(settings.isNotificationsEnabled());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            int userId = SessionManager.getCurrentUserId();
            
            // Update user profile
            if (currentUser != null) {
                currentUser.setName(nameField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                
                try {
                    if (!monthlyIncomeField.getText().trim().isEmpty()) {
                        currentUser.setMonthlyIncome(Double.parseDouble(monthlyIncomeField.getText().trim()));
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid monthly income");
                    return;
                }
                
                try {
                    if (!hourlyWageField.getText().trim().isEmpty()) {
                        currentUser.setHourlyWage(Double.parseDouble(hourlyWageField.getText().trim()));
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid hourly wage");
                    return;
                }
                
                userDAO.update(currentUser);
                SessionManager.setCurrentUser(currentUser);
            }
            
            // Update settings
            settings.setTheme(themeCombo.getValue());
            settings.setCurrency(currencyCombo.getValue());
            settings.setNotificationsEnabled(notificationsCheckbox.isSelected());
            settingsDAO.update(settings);
            
            showSuccess("Settings saved successfully!");
        } catch (Exception e) {
            showError("Error saving settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
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


