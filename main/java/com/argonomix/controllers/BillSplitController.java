package com.argonomix.controllers;

import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class BillSplitController implements Initializable {
    
    @FXML private VBox groupsContainer;
    
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
        
        loadGroups();
    }
    
    private void loadGroups() {
        // TODO: Implement bill split groups loading
        groupsContainer.getChildren().clear();
        
        Label noGroupsLabel = new Label("No groups yet. Create your first bill split group!");
        noGroupsLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 20px;");
        groupsContainer.getChildren().add(noGroupsLabel);
    }
    
    @FXML
    private void handleCreateGroup() {
        // TODO: Implement group creation modal
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create Group");
        alert.setHeaderText("Bill Split Group Form");
        alert.setContentText("Group creation modal will be implemented here.");
        alert.showAndWait();
    }
}


