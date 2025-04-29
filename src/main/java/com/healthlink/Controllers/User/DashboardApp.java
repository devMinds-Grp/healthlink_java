package com.healthlink.Controllers.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class DashboardApp {

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/User/Dashboard.fxml"));
        primaryStage.setTitle("Mon Dashboard Power BI");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
