package com.argonomix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Check if user is logged in
        com.argonomix.utils.SessionManager sessionManager = new com.argonomix.utils.SessionManager();
        String fxmlFile = "/fxml/Login.fxml";
        
        // If user is logged in, check onboarding status
        if (com.argonomix.utils.SessionManager.isLoggedIn()) {
            try {
                com.argonomix.database.UserSettingsDAO settingsDAO = new com.argonomix.database.UserSettingsDAO();
                com.argonomix.models.UserSettings settings = settingsDAO.getOrCreate(
                    com.argonomix.utils.SessionManager.getCurrentUserId()
                );
                if (!settings.isOnboardingComplete()) {
                    fxmlFile = "/fxml/Onboarding.fxml";
                } else {
                    fxmlFile = "/fxml/Dashboard.fxml";
                }
            } catch (Exception e) {
                // Default to login if error
                fxmlFile = "/fxml/Login.fxml";
            }
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/modular.css").toExternalForm());
        
        primaryStage.setTitle("ARGonomix - Personal Finance Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Note: Min size constraints removed to avoid macOS NSTrackingArea crash
        // This is a known JavaFX 17 bug on macOS
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

