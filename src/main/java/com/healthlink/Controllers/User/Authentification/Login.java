package com.healthlink.Controllers.User.Authentification;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;


public class Login {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button resetButton;

    private final AuthService AuthService = new AuthService();

    @FXML
    void initialize() {
        loginButton.setOnAction(this::handleLogin);
        resetButton.setOnAction(event -> {
            emailField.clear();
            passwordField.clear();
        });
    }

    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur utilisateur = AuthService.login(email, password);
            if (utilisateur != null) {
                if (!"approuvé".equalsIgnoreCase(utilisateur.getStatut())) {
                    // Afficher une page ou alerte pour statut non approuvé
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Auth/NonApprouve.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                    return;
                }

                // 1. STOCKER L'UTILISATEUR CONNECTÉ DANS LE SERVICE
                AuthService.setConnectedUtilisateur(utilisateur);

                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie", "Bienvenue " + utilisateur.getPrenom() + " !");

                // 2. REDIRECTION VERS LA PAGE D'ACCUEIL
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();


            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de connexion", "Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite : " + e.getMessage());
        }
    }


    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleRegisterLink(ActionEvent event) {
        loadView("/views/User/Auth/register.fxml", event);
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
}
