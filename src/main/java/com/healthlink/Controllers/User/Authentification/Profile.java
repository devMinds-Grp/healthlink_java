package com.healthlink.Controllers.User.Authentification;

import com.healthlink.Controllers.User.Modifier;
import com.healthlink.Controllers.User.ModifierMedecin;
import com.healthlink.Controllers.User.ModifierSoignant;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Profile implements Initializable {
    @FXML
    private Button dashboardButton;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label connexionLabel;
    @FXML
    private ImageView profileImageView; // Ajout du ImageView


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserData();
        // Récupère l'utilisateur connecté depuis AuthService
        Utilisateur utilisateur = AuthService.getConnectedUtilisateur();


    }

    private void loadProfileImage(Utilisateur user) {
        try {
            // 1. Vérifier si l'utilisateur a une image
            if (user.getImageprofile() != null && !user.getImageprofile().isEmpty()) {

                // 2. Construire le chemin relatif
                File imageFile = new File("profile_images/" + user.getImageprofile());

                // 3. Debug: Afficher le chemin absolu pour vérification
                System.out.println("Chemin de l'image: " + imageFile.getAbsolutePath());

                // 4. Vérifier que le fichier existe
                if (imageFile.exists()) {
                    // 5. Charger avec URI (méthode la plus fiable)
                    String imageUrl = imageFile.toURI().toString();
                    profileImageView.setImage(new Image(imageUrl));
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur de chargement: " + e.getMessage());

        }

    }

    private void loadUserData() {
        Utilisateur utilisateur = AuthService.getConnectedUtilisateur();

        if (utilisateur != null) {
            nomField.setText(utilisateur.getNom());
            prenomField.setText(utilisateur.getPrenom());
            emailField.setText(utilisateur.getEmail());
            usernameLabel.setText(utilisateur.getNom() + " " + utilisateur.getPrenom());
            connexionLabel.setText("Vous êtes connecté en tant que " + utilisateur.getEmail());

            loadProfileImage(utilisateur);
        } else {
            System.out.println("Aucun utilisateur connecté !");
        }
    }

    public void handleEditProfile() {
        try {
            Utilisateur utilisateur = AuthService.getConnectedUtilisateur();
            if (utilisateur == null) {
                showAlert("Erreur", "Aucun utilisateur connecté", Alert.AlertType.ERROR);
                return;
            }

            // Charger le formulaire approprié selon le rôle
            String fxmlFile;
            Class<?> controllerClass;

            switch (utilisateur.getRole().getId()) {
                case 2: // Médecin
                    fxmlFile = "/views/User/modifierMedecin.fxml";
                    controllerClass = ModifierMedecin.class;
                    break;
                case 3: // Patient
                    fxmlFile = "/views/User/modifier.fxml";
                    controllerClass = Modifier.class;
                    break;
                case 4: // Soignant
                    fxmlFile = "/views/User/modifierSoignant.fxml";
                    controllerClass = ModifierSoignant.class;
                    break;
                default:
                    showAlert("Erreur", "Rôle utilisateur non reconnu", Alert.AlertType.ERROR);
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Récupérer le contrôleur après le load()
            Object controller = loader.getController();

            // Initialiser les données selon le type de contrôleur
            if (controller instanceof Modifier) {
                ((Modifier) controller).initData(utilisateur);
            } else if (controller instanceof ModifierMedecin) {
                ((ModifierMedecin) controller).initData(utilisateur);
            } else if (controller instanceof ModifierSoignant) {
                ((ModifierSoignant) controller).initData(utilisateur);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier le profil - " + utilisateur.getRole().getNom());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recharger les données après la modification
            loadUserData();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de modification", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur inattendue est survenue", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode générique pour charger les vues
    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            // 1. Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Obtenir la stage actuelle à partir de l'événement
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

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