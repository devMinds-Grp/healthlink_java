package com.healthlink.Controllers.User.Authentification;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class ConfirmationController {

    @FXML
    public void initialize() {
        // Redirection automatique après 15 secondes
        new Thread(() -> {
            try {
                Thread.sleep(15000);
                javafx.application.Platform.runLater(() -> handleRetour(null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/register.fxml")); // à ajuster selon le nom réel
            Parent root = loader.load();
            Stage stage = (Stage) (event != null ? ((Node) event.getSource()).getScene().getWindow() : root.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
