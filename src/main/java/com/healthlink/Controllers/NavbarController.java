package com.healthlink.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarController {

    @FXML private Button homeButton;
    @FXML private Button careButton;
    @FXML private Button prescriptionButton;
    @FXML private Button profileButton;
    @FXML private Button disconnectButton;

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            stage.setWidth(screenSize.getWidth());
            stage.setHeight(screenSize.getHeight());
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    @FXML
    private void navigateToHome() {
        navigateTo("/views/Home.fxml", "Accueil");
    }

    @FXML
    private void navigateToCare() {
        navigateTo("/views/care/ListCare.fxml", "Liste des Soins");
    }

    @FXML
    private void navigateToPrescription() {
        navigateTo("/views/Prescription/list_prescription.fxml", "Liste des Prescriptions");
    }
    @FXML
    private void navigateToAppointment() {
        navigateTo("/views/Appointment/list_appointments.fxml", "Liste des Rendez-vous");
    }

    @FXML
    private void navigateToProfile() {
        navigateTo("/views/Profile.fxml", "Profil");
    }

    @FXML
    private void navigateToDisconnect() {
        // Implement logout logic (e.g., clear session, redirect to login)
        navigateTo("/views/Login.fxml", "Connexion");
    }
    @FXML
    private void navigateToDon() {
        navigateTo("/views/list_don.fxml", "Donation");
    }
    @FXML
    private void navigateToForum() {
        navigateTo("/views/MainView.fxml", "Forum");
    }
    @FXML
    private void navigateToReclamations() {
        navigateTo("/list_reclamations.fxml", "Reclamations");
    }


    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}