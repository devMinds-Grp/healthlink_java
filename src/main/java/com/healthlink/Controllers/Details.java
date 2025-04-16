package com.healthlink.Controllers;
import com.healthlink.Entites.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Details {
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telLabel;
    @FXML private Label adresseLabel;
    @FXML private Label statutLabel;

    public void initData(Utilisateur patient) {
        try {
            nomLabel.setText(patient.getNom());
            prenomLabel.setText(patient.getPrenom());
            emailLabel.setText(patient.getEmail());
            telLabel.setText(String.valueOf(patient.getNum_tel()));
            adresseLabel.setText(patient.getAdresse() != null ? patient.getAdresse() : "Non renseignée");
            statutLabel.setText(patient.getStatut() != null ? patient.getStatut() : "Inconnu");
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