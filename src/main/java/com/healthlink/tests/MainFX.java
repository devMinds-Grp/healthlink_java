package com.healthlink.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));

        // Verify database connection first
        try {
            com.healthlink.utils.MyDB.getInstance().getConnection();
            System.out.println("Database connected successfully");
        } catch (Exception e) {
            System.err.println("DATABASE CONNECTION FAILED:");
            e.printStackTrace();
            showAlert("Database Error", "Cannot connect to database");
            return;
        }

        // Load FXML
        Parent root = FXMLLoader.load(getClass().getResource("/Liste_categories.fxml"));
        primaryStage.setTitle("Care Management System");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        // This will be called by MainLauncher
        launch(args);
    }
}