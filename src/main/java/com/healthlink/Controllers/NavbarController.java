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
import java.util.HashMap;
import java.util.Map;

public class NavbarController {

    @FXML private Button homeButton;
    @FXML private Button careButton;
    @FXML private Button prescriptionButton;
    @FXML private Button appointmentButton;
    @FXML private Button donButton;
    @FXML private Button donationResponseButton;
    @FXML private Button hospitalsButton;
    @FXML private Button ForumButton;
    @FXML private Button ReclamationButton;
    @FXML private Button profileButton;
    @FXML private Button disconnectButton;
    @FXML private Button dashboardButton;

    // Map to store navigation details
    private static final Map<String, NavigationTarget> NAVIGATION_MAP = new HashMap<>();

    static {
        NAVIGATION_MAP.put("home", new NavigationTarget("/views/Home.fxml", "Accueil"));
        NAVIGATION_MAP.put("care", new NavigationTarget("/views/care/ListCare.fxml", "Liste des Soins"));
        NAVIGATION_MAP.put("prescription", new NavigationTarget("/views/Prescription/list_prescription.fxml", "Liste des Prescriptions"));
        NAVIGATION_MAP.put("appointment", new NavigationTarget("/views/Appointment/list_appointments.fxml", "Liste des Rendez-vous"));
        NAVIGATION_MAP.put("don", new NavigationTarget("/views/list_don.fxml", "Donation"));
        NAVIGATION_MAP.put("donationResponse", new NavigationTarget("/views/list_reponse_don.fxml", "Donation Response List"));
        NAVIGATION_MAP.put("hospitals", new NavigationTarget("/views/map_view.fxml", "Find Nearby Hospitals"));
        NAVIGATION_MAP.put("forum", new NavigationTarget("/views/MainView.fxml", "Forum"));
        NAVIGATION_MAP.put("reclamations", new NavigationTarget("/list_reclamations.fxml", "Reclamations"));
        NAVIGATION_MAP.put("dashboard", new NavigationTarget("/views/User/list.fxml", "Tableau de bord"));
        NAVIGATION_MAP.put("profile", new NavigationTarget("/views/User/Profile.fxml", "Profil"));
        NAVIGATION_MAP.put("disconnect", new NavigationTarget("/views/User/Auth/Login.fxml", "Connexion"));
    }

    // Static class to hold navigation target details
    private static class NavigationTarget {
        String fxmlPath;
        String title;

        NavigationTarget(String fxmlPath, String title) {
            this.fxmlPath = fxmlPath;
            this.title = title;
        }
    }

    @FXML
    public void initialize() {
        Utilisateur utilisateur = AuthService.getConnectedUtilisateur();
        Button[] allButtons = {
                homeButton, careButton, prescriptionButton, appointmentButton, donButton,
                donationResponseButton, hospitalsButton, ForumButton, ReclamationButton,
                profileButton, disconnectButton, dashboardButton
        };

        // Hide all buttons by default
        setButtonVisibility(false, allButtons);

        // If no user is logged in, return early
        if (utilisateur == null) {
            return;
        }

        // Show buttons based on user role
        switch (utilisateur.getRole().getId()) {
            case 1: // Admin
                setButtonVisibility(true, homeButton, dashboardButton, prescriptionButton,
                        ForumButton, profileButton, ReclamationButton, donButton, disconnectButton);
                break;
            case 2: // Doctor
                setButtonVisibility(true, homeButton, prescriptionButton, hospitalsButton,
                        ForumButton, ReclamationButton, profileButton, disconnectButton);
                break;
            case 3: // Patient
                setButtonVisibility(true, homeButton, appointmentButton, donButton,
                        donationResponseButton, hospitalsButton, ForumButton, ReclamationButton,
                        profileButton, disconnectButton);
                break;
            case 4: // Soignant
                setButtonVisibility(true, homeButton, careButton, hospitalsButton,
                        ForumButton, ReclamationButton, profileButton, disconnectButton);
                break;
            default:
                // No buttons visible for unrecognized roles
                break;
        }
    }

    // Utility method to set button visibility and managed property
    private void setButtonVisibility(boolean visible, Button... buttons) {
        for (Button button : buttons) {
            if (button != null) {
                button.setVisible(visible);
                button.setManaged(visible);
            }
        }
    }

    // Centralized navigation method
    private void navigateTo(String targetKey) {
        NavigationTarget target = NAVIGATION_MAP.get(targetKey);
        if (target == null) {
            showAlert("Erreur", "Cible de navigation inconnue.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(target.fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(target.title);
            stage.setMaximized(true);
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            stage.setWidth(screenSize.getWidth());
            stage.setHeight(screenSize.getHeight());
            stage.setResizable(true);
            stage.show();

            // Handle logout for disconnect action
            if ("disconnect".equals(targetKey)) {
                AuthService.logout();
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // Navigation methods
    @FXML private void navigateToHome() { navigateTo("home"); }
    @FXML private void navigateToCare() { navigateTo("care"); }
    @FXML private void navigateToPrescription() { navigateTo("prescription"); }
    @FXML private void navigateToAppointment() { navigateTo("appointment"); }
    @FXML private void navigateToDon() { navigateTo("don"); }
    @FXML private void navigateToDonationResponse() { navigateTo("donationResponse"); }
    @FXML private void navigateToHospitals() { navigateTo("hospitals"); }
    @FXML private void navigateToForum() { navigateTo("forum"); }
    @FXML private void navigateToReclamations() { navigateTo("reclamations"); }
    @FXML private void navigateToDashboard() { navigateTo("dashboard"); }
    @FXML private void navigateToProfile() { navigateTo("profile"); }
    @FXML private void navigateToDisconnect() { navigateTo("disconnect"); }

    // Utility method to show error alerts
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}