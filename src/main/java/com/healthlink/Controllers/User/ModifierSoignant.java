package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ModifierSoignant {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField categorieField;
    @FXML
    private ComboBox<String> specialiteComboBox;
    @FXML
    private TextField motdepasseTextField;
    @FXML
    private Label nomFichierLabel;
    private File fichierImage;
    @FXML
    private Button choisirImageButton;
    @FXML
    private Button choisirDiplomeButton;
    @FXML
    private Label nomDiplomeLabel;
    private File fichierDiplome;

    private final UserService userService = new UserService();
    private Utilisateur soignant;

    public void initData(Utilisateur soignant) {
        this.soignant = soignant;
        remplirChamps();
    }

    private void remplirChamps() {
        nomField.setText(soignant.getNom());
        prenomField.setText(soignant.getPrenom());
        emailField.setText(soignant.getEmail());
        motdepasseTextField.setText(soignant.getMot_de_passe());

//        categorieField.setText(soignant.getCategorie_soin());
    }

    @FXML
    private void handleSave() {
        try {
            if (!validerChamps()) return;

            mettreAJourSoignant();
            userService.updateSoignant(soignant);

            showAlert(AlertType.INFORMATION, "Succès", "Soignant modifié avec succès");
            fermerFenetre();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private boolean validerChamps() {
        // Validation des champs obligatoires
        if (!validateRequiredField(nomField.getText(), "nom") ||
                !validateRequiredField(prenomField.getText(), "prénom") ||
                !validateEmail(emailField.getText()) ) {
            return false;
        }
        return true;
    }

    private boolean validateRequiredField(String field, String fieldName) {
        if (field == null || field.trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Le champ " + fieldName + " est obligatoire");
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (email == null || !email.matches(emailRegex)) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Email invalide");
            return false;
        }
        return true;
    }

    private boolean validateCategorieSoin(String categorie) {
        if (categorie == null || categorie.trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "La catégorie de soin est obligatoire");
            return false;
        }

        // Validation supplémentaire pour la longueur minimale
        if (categorie.length() < 3) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "La catégorie de soin doit contenir au moins 3 caractères");
            return false;
        }

        return true;
    }

    private void mettreAJourSoignant() {
        soignant.setNom(nomField.getText());
        soignant.setPrenom(prenomField.getText());
        soignant.setEmail(emailField.getText());
        //soignant.setCategorie_soin(categorieField.getText());
        soignant.setMot_de_passe(motdepasseTextField.getText());
        // Gestion de l'image de profil
        if (fichierImage != null) {
            // Sauvegarder le chemin de l'image
            soignant.setImageprofile(fichierImage.getAbsolutePath());

            // Optionnel: Copier l'image dans un dossier spécifique
            String destinationPath = saveProfileImage(fichierImage);
            soignant.setImageprofile(destinationPath);
        }
        if (fichierDiplome != null) {
            String diplomePath = saveDiplome(fichierDiplome);
            soignant.setImage(diplomePath); // Utilisez la variable diplomePath qui vient d'être créée
        }
    }

    @FXML
    private void handleCancel() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        nomField.getScene().getWindow().hide();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleChoisirDiplome() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier de diplôme");

        // Filtres pour les types de fichiers (PDF, images, etc.)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) choisirDiplomeButton.getScene().getWindow();
        fichierDiplome = fileChooser.showOpenDialog(stage);

        if (fichierDiplome != null) {
            nomDiplomeLabel.setText(fichierDiplome.getName());
        } else {
            nomDiplomeLabel.setText("Aucun fichier choisi");
        }
    }

    private String saveDiplome(File diplomeFile) {
        try {
            // Créer un dossier diplomes s'il n'existe pas
            File dossierDiplomes = new File("diplomes");
            if (!dossierDiplomes.exists()) {
                dossierDiplomes.mkdir();
            }

            // Générer un nom unique pour le fichier
            String nomFichier = System.currentTimeMillis() + "_" + diplomeFile.getName();
            File destination = new File(dossierDiplomes, nomFichier);

            // Copier le fichier
            java.nio.file.Files.copy(
                    diplomeFile.toPath(),
                    destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return nomFichier;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @FXML
    private void handleChoisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de profil");

        // Filtres pour les types de fichiers images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) choisirImageButton.getScene().getWindow();
        fichierImage = fileChooser.showOpenDialog(stage);

        if (fichierImage != null) {
            nomFichierLabel.setText(fichierImage.getName());
        } else {
            nomFichierLabel.setText("Aucun fichier choisi");
        }
    }
    private String saveProfileImage(File imageFile) {
        try {
            // Créer un dossier images s'il n'existe pas
            File dossierImages = new File("profile_images");
            if (!dossierImages.exists()) {
                dossierImages.mkdir();
            }

            // Générer un nom unique pour le fichier
            String nomFichier = System.currentTimeMillis() + "_" + imageFile.getName();
            File destination = new File(dossierImages, nomFichier);

            // Copier le fichier
            java.nio.file.Files.copy(
                    imageFile.toPath(),
                    destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return nomFichier;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}