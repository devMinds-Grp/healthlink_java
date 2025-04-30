package com.healthlink;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ForumApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Charge MainView.fxml comme conteneur racine
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
        Parent root = loader.load();

        // Configure la scène principale
        Scene scene = new Scene(root, 1000, 600); // Taille ajustable
        scene.getStylesheets().add(getClass().getResource("/styles/forum.css").toExternalForm());

        // Affiche la fenêtre
        stage.setScene(scene);
        stage.setTitle("HealthLink");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}