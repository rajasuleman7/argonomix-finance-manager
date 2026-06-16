package com.argonomix.controllers;

import com.argonomix.database.UserDAO;
import com.argonomix.models.User;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink signupLink;
    @FXML private Label errorLabel;
    
    private UserDAO userDAO;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        errorLabel.setVisible(false);
    }
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }
        
        try {
            User user = userDAO.authenticate(email, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                NavigationUtil.switchToScene("/fxml/Dashboard.fxml");
            } else {
                showError("Invalid email or password");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
            System.out.println("---------------An error occurred:----------- " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSignupLink() {
        try {
            NavigationUtil.switchToScene("/fxml/Signup.fxml");
        } catch (Exception e) {
            showError("Error loading signup page: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}

