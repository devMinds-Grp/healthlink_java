package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Modifier {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField motdepasseTextField;
    @FXML
    private Button choisirImageButton;
    private File fichierImage;
    @FXML
    private Label nomFichierLabel;

    private final UserService userService = new UserService();
    private Utilisateur patient;

    public void initData(Utilisateur patient) {
        this.patient = patient;
        remplirChamps();
    }

    private void remplirChamps() {
        nomField.setText(patient.getNom());
        prenomField.setText(patient.getPrenom());
        emailField.setText(patient.getEmail());
        telField.setText(String.valueOf(patient.getNum_tel()));
        motdepasseTextField.setText(patient.getMot_de_passe());
    }

    @FXML
    private void handleSave() {
        try {
            if (!validerChamps()) return;

            mettreAJourPatient();
            userService.updatePatient(patient);

            showAlert(AlertType.INFORMATION, "Succès", "Patient modifié avec succès");
            fermerFenetre();
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Erreur", "Le numéro de téléphone doit être un nombre valide");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private boolean validerChamps() {
        // Validation des champs obligatoires
        if (!validateRequiredField(nomField.getText(), "nom") ||
                !validateRequiredField(prenomField.getText(), "prénom") ||
                !validateEmail(emailField.getText()) ||
                !validatePhone(telField.getText()) ||
                !validatePassword(motdepasseTextField.getText())) {
            return false;
        }
        return true;
    }
    private boolean validatePassword(String password) {
        // Au moins 8 caractères, une majuscule, une minuscule, un chiffre
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (password == null || !password.matches(passwordRegex)) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Le mot de passe doit contenir:\n- Au moins 8 caractères\n- Une majuscule\n- Une minuscule\n- Un chiffre");
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

    private boolean validatePhone(String phone) {
        try {
            if (phone == null || phone.length() != 8) {
                showAlert(AlertType.ERROR, "Erreur de saisie", "Le numéro de téléphone doit contenir 8 chiffres");
                return false;
            }
            Integer.parseInt(phone);
            return true;
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Erreur de saisie", "Le numéro de téléphone ne doit contenir que des chiffres");
            return false;
        }
    }

    private void mettreAJourPatient() {
        patient.setNom(nomField.getText());
        patient.setPrenom(prenomField.getText());
        patient.setEmail(emailField.getText());
        patient.setNum_tel(Integer.parseInt(telField.getText()));
        patient.setMot_de_passe(motdepasseTextField.getText());
        // Gestion de l'image de profil
        if (fichierImage != null) {
            // Sauvegarder le chemin de l'image
            patient.setImageprofile(fichierImage.getAbsolutePath());

            // Optionnel: Copier l'image dans un dossier spécifique
            String destinationPath = saveProfileImage(fichierImage);
            patient.setImageprofile(destinationPath);
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
}