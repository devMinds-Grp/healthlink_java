package com.healthlink.Controllers;

import com.healthlink.Entites.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DetailsSoignant {
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    //@FXML private Label telLabel;
    @FXML private Label categorieLabel;
    //@FXML private Label statutLabel;

    public void initData(Utilisateur soignant) {
        try {
            nomLabel.setText(soignant.getNom());
            prenomLabel.setText(soignant.getPrenom());
            emailLabel.setText(soignant.getEmail());
            //telLabel.setText(String.valueOf(soignant.getNum_tel()));
            categorieLabel.setText(soignant.getCategorie_soin());
            //statutLabel.setText(soignant.getStatut() != null ? soignant.getStatut() : "Inconnu");
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