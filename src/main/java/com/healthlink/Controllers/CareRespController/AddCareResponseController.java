package com.healthlink.Controllers.CareRespController;

import com.healthlink.Entities.Care;
import com.healthlink.Entities.CareResponse;
import com.healthlink.Entities.Notification;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
import com.healthlink.Services.CareService;
import com.healthlink.Services.CareResponseService;
import com.healthlink.Services.NotificationService;
import com.healthlink.utils.MyDB;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddCareResponseController {


    @FXML private TextArea contenuRepField;

    private int careId;
    private final CareResponseService responseService;
    private final CareService careService;
    private final NotificationService notificationService;

    public AddCareResponseController() {
        try {
            this.responseService = new CareResponseService(MyDB.getInstance().getConnection());
            this.careService = new CareService(MyDB.getInstance().getConnection());
            this.notificationService = new NotificationService(MyDB.getInstance().getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize services", e);
        }
    }



    public void setCareId(int careId) {
        this.careId = careId;
        System.out.println("Set careId to " + careId);
    }

    @FXML
    private void handleSave() {
        try {
            // Get inputs

            String contenuRep = contenuRepField.getText().trim();

            // Validate inputs


            if (contenuRep.isEmpty()) {
                showAlert("Champ requis", "Veuillez entrer le contenu de la réponse.", Alert.AlertType.WARNING);
                return;
            }
            if (contenuRep.length() < 10) {
                showAlert("Erreur", "Le contenu doit contenir au moins 10 caractères.", Alert.AlertType.WARNING);
                return;
            }

            // Create CareResponse object
            CareResponse response = new CareResponse();
            response.setDateRep(LocalDate.now()); // Automatically use today's date
            response.setContenuRep(contenuRep);
            response.setCareId(careId); // Explicitly set careId

            // Set soignant (logged-in user)
            Utilisateur soignant = AuthService.getConnectedUtilisateur();
            if (soignant == null || soignant.getRole().getId() != 4) {
                showAlert("Erreur", "Seul un soignant peut ajouter une réponse.", Alert.AlertType.ERROR);
                return;
            }
            response.setUser(soignant);
            response.setSoignant(soignant);
            System.out.println("Adding response for soignant: ID=" + soignant.getId() +
                    ", Name=" + (soignant.getPrenom() != null ? soignant.getPrenom() : "null") + " " +
                    (soignant.getNom() != null ? soignant.getNom() : "null"));

            // Fetch Care to get patient
            Care care = careService.getCareById(careId);
            if (care == null) {
                showAlert("Erreur", "Le soin associé n'existe pas.", Alert.AlertType.ERROR);
                System.err.println("Care not found for ID: " + careId);
                return;
            }
            response.setCare(care);
            response.setPatient(care.getPatient());
            if (care.getPatient() == null) {
                showAlert("Erreur", "Aucun patient associé au soin.", Alert.AlertType.ERROR);
                System.err.println("No patient associated with care ID: " + careId);
                return;
            }
            System.out.println("Setting patient for response: ID=" + care.getPatient().getId() +
                    ", Name=" + (care.getPatient().getPrenom() != null ? care.getPatient().getPrenom() : "null") + " " +
                    (care.getPatient().getNom() != null ? care.getPatient().getNom() : "null"));

            // Log the CareResponse details before saving
            System.out.println("Saving CareResponse: care_id=" + response.getCareId() +
                    ", user_id=" + (response.getUser() != null ? response.getUser().getId() : "null") +
                    ", patient_id=" + (response.getPatient() != null ? response.getPatient().getId() : "null") +
                    ", soignant_id=" + (response.getSoignant() != null ? response.getSoignant().getId() : "null") +
                    ", date_rep=" + response.getDateRep() +
                    ", contenu_rep=" + response.getContenuRep());

            // Save to database and get the generated CareResponse ID
            int careResponseId = responseService.addCareResponseAndGetId(response, careId);
            if (careResponseId > 0) {
                // Create notification for patient
                Notification notification = new Notification();
                notification.setUser(care.getPatient());
                String soignantName = (soignant.getPrenom() != null ? soignant.getPrenom() : "") + " " +
                        (soignant.getNom() != null ? soignant.getNom() : "");
                notification.setMessage("A soignant (" + soignantName.trim() + ") has responded to your care request.");
                notification.setCreatedAt(LocalDateTime.now());
                notification.setRead(false);
                notification.setCare(care);

                boolean notificationSuccess = notificationService.addNotification(notification);
                if (notificationSuccess) {
                    System.out.println("Notification created successfully for patient ID=" + care.getPatient().getId() +
                            ", Message: " + notification.getMessage());
                } else {
                    System.err.println("Failed to create notification for patient ID=" + care.getPatient().getId());
                    showAlert("Avertissement", "Réponse ajoutée, mais échec de la création de la notification.", Alert.AlertType.WARNING);
                }

                showAlert("Succès", "Réponse ajoutée avec succès.", Alert.AlertType.INFORMATION);
                handleCancel();
            } else {
                showAlert("Erreur", "Échec de l'ajout de la réponse.", Alert.AlertType.ERROR);
                System.err.println("Failed to add CareResponse for care ID: " + careId);
            }
        } catch (Exception e) {
            showAlert("Erreur Critique", "Une erreur inattendue s'est produite : " + e.getMessage(), Alert.AlertType.ERROR);
            System.err.println("Exception in handleSave: " + e.getMessage());
        }
    }
    @FXML
    private void handleCancel() {
        // Use any of your remaining components to get the stage
        Stage stage = (Stage) contenuRepField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    getClass().getResource("/css/Care.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("custom-alert");

            alert.showAndWait();
        });
    }
}