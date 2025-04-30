package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DetailsMedecin {
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telLabel;
    @FXML private Label specialiteLabel;
    @FXML private Label adresseLabel;
    @FXML private Label nomDiplomeLabel;
    public void initData(Utilisateur medecin) {
        try {
            nomLabel.setText(medecin.getNom());
            prenomLabel.setText(medecin.getPrenom());
            emailLabel.setText(medecin.getEmail());
            telLabel.setText(String.valueOf(medecin.getNum_tel()));
            specialiteLabel.setText(medecin.getSpeciality());
            adresseLabel.setText(medecin.getAdresse() != null ? medecin.getAdresse() : "Non renseignée");

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible d'afficher les détails: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        nomLabel.getScene().getWindow().hide();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}