package com.healthlink.Controllers;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
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
    @FXML private Button appointmentButton;
    @FXML private Button doctorButton; // New button for "Médecins"
    @FXML private Button myAppointmentsButton; // New button for "Mes RDV"
    @FXML private Button donButton;
    @FXML private Button ForumButton;
    @FXML private Button ReclamationButton;
    @FXML private Button profileButton;
    @FXML private Button disconnectButton;
    @FXML private Button dashboardButton;

    @FXML
    public void initialize() {
        // Récupère l'utilisateur connecté depuis AuthService
        Utilisateur utilisateur = AuthService.getConnectedUtilisateur();

        // Vérifie si l'utilisateur est connecté
        if (utilisateur == null) {
            // Cacher tous les boutons si aucun utilisateur n'est connecté
            setButtonVisibility(false, homeButton, careButton, prescriptionButton, appointmentButton,
                    doctorButton, myAppointmentsButton, donButton, ForumButton, ReclamationButton,
                    profileButton, disconnectButton, dashboardButton);
            return;
        }

        // Par défaut, cacher tous les boutons
        setButtonVisibility(false, homeButton, careButton, prescriptionButton, appointmentButton,
                doctorButton, myAppointmentsButton, donButton, ForumButton, ReclamationButton,
                profileButton, disconnectButton, dashboardButton);

        // Afficher les boutons en fonction du rôle de l'utilisateur
        switch (utilisateur.getRole().getId()) {
            case 1: // Admin
                setButtonVisibility(true, homeButton, dashboardButton, prescriptionButton, ForumButton,
                        profileButton, ReclamationButton, donButton, disconnectButton);
                break;
            case 2: // Doctor
                setButtonVisibility(true, homeButton, prescriptionButton, myAppointmentsButton,
                        ForumButton, ReclamationButton, profileButton, disconnectButton);
                break;
            case 3: // Patient
                setButtonVisibility(true, homeButton, appointmentButton, doctorButton, donButton,
                        ForumButton, ReclamationButton, profileButton, disconnectButton);
                break;
            case 4: // Soignant
                setButtonVisibility(true, homeButton, careButton, ForumButton, ReclamationButton,
                        profileButton, disconnectButton);
                break;
            default:
                // Aucun bouton visible pour les rôles non reconnus
                break;
        }
    }

    // Méthode utilitaire pour gérer la visibilité et l'espace des boutons
    private void setButtonVisibility(boolean visible, Button... buttons) {
        for (Button button : buttons) {
            if (button != null) {
                button.setVisible(visible);
                button.setManaged(visible); // Empêche de laisser l’espace vide
            }
        }
    }

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
    private void navigateToDoctors() {
        navigateTo("/views/Appointment/list_doctor.fxml", "Liste des Médecins");
    }

    @FXML
    private void navigateToMyAppointments() {
        navigateTo("/views/Appointment/my_appointments.fxml", "Mes Rendez-vous");
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

    @FXML
    private void navigateToDashboard() {
        navigateTo("/views/User/list.fxml", "Tableau de bord");
    }

    @FXML
    private void navigateToProfile() {
        navigateTo("/views/User/Profile.fxml", "Profil");
    }

    @FXML
    private void navigateToDisconnect() {
        AuthService.logout();
        navigateTo("/views/User/Auth/Login.fxml", "Connexion");
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}