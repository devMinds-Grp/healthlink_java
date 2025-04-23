package com.healthlink.Controllers.User.Authentification;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NonApprouveController {
    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                Thread.sleep(15000);
                javafx.application.Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/login.fxml"));
                        Parent root = loader.load();
                        //Stage stage = (Stage) loader.getRoot().getScene().getWindow();
                        //stage.setScene(new Scene(root));
                        //stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/login.fxml")); // ajuste le chemin si besoin
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
