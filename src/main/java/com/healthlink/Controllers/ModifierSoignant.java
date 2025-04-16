package com.healthlink.Controllers;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ModifierSoignant {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField categorieField;

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
        categorieField.setText(soignant.getCategorie_soin());
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
                !validateEmail(emailField.getText()) ||
                !validateCategorieSoin(categorieField.getText())) {
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
        soignant.setCategorie_soin(categorieField.getText());
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
}