package com.argonomix.utils;

import com.argonomix.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class NavigationUtil {
    
    public static void switchScene(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(NavigationUtil.class.getResource("/css/modular.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void switchScene(String fxmlFile, Node node) throws IOException {
        Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlFile));
        Stage stage = (Stage) node.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(NavigationUtil.class.getResource("/css/modular.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void switchToScene(String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlFile));
        Stage stage = Main.getPrimaryStage();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(NavigationUtil.class.getResource("/css/modular.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static Stage createModalStage(String fxmlFile, String title) throws IOException {
        Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlFile));
        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(NavigationUtil.class.getResource("/css/modular.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        return stage;
    }
}

