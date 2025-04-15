package com.healthlink.Controllers;

import com.healthlink.Entites.User;
import com.healthlink.Services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Modifier {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField adresseField;

    private final UserService userService = new UserService();
    private User patient;

    public void initData(User patient) {
        this.patient = patient;
        remplirChamps();
    }

    private void remplirChamps() {
        nomField.setText(patient.getNom());
        prenomField.setText(patient.getPrenom());
        emailField.setText(patient.getEmail());
        telField.setText(String.valueOf(patient.getNum_tel()));
        adresseField.setText(patient.getAdresse());
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
                !validatePhone(telField.getText())) {
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
        patient.setAdresse(adresseField.getText());
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