package com.healthlink.Controllers;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    public void initialize() {
        // No button visibility logic needed here, handled by NavbarController
    }

    @FXML
    private void handleProfile(ActionEvent event) {

        loadView("/views/User/Auth/Profile.fxml", event);
    }
    @FXML
    private void handleDashboard(ActionEvent event) {
        loadView("/views/User/list.fxml", event);

    }

    @FXML
    private void handleHome(ActionEvent event) {
        loadView("/views/Home.fxml", event);
    }

    // Méthode générique pour charger les vues
    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue: " + fxmlPath);
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            // Nettoyer la session
            AuthService.logout();
            loadView("/views/Login.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}