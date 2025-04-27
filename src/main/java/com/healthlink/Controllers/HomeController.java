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
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    @FXML
    private Button dashboardButton;

    @FXML
    public void initialize() {
        // Récupère l'utilisateur connecté depuis AuthService
        Utilisateur utilisateur = AuthService.getConnectedUtilisateur();

        // Vérifie si l'utilisateur est connecté et que son rôle est ADMIN (id=1)
        if (utilisateur == null || utilisateur.getRole().getId() != 1) {
            dashboardButton.setVisible(false);
            dashboardButton.setManaged(false); // Empêche de laisser l’espace vide
        }
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
            // 1. Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Obtenir la stage actuelle à partir de l'événement
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // 3. Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue: " + fxmlPath);

            // Optionnel: Afficher une alerte à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger la vue");
            alert.setContentText("Le fichier " + fxmlPath + " est introuvable ou corrompu.");
            alert.showAndWait();
        }
    }
    @FXML
    void handleLogout(ActionEvent event) {
        try {
            // 1. Nettoyer la session
            AuthService.logout();

            // 2. Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/Login.fxml"));
            Parent root = loader.load();

            // 3. Récupérer la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 4. Changer la scène
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
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
