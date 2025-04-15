package com.healthlink.Controllers.CareRespController;

import com.healthlink.Entities.CareResponse;
import com.healthlink.Services.CareResponseService;
import com.healthlink.utils.MyDB;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class AddCareResponseController {

    @FXML private DatePicker dateRepField;
    @FXML private TextArea contenuRepField;

    private int careId;
    private final CareResponseService responseService;

    public AddCareResponseController() {
        try {
            this.responseService = new CareResponseService(MyDB.getInstance().getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize CareResponseService", e);
        }
    }

    @FXML
    public void initialize() {
        // Restrict DatePicker to today or future dates
        dateRepField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    public void setCareId(int careId) {
        this.careId = careId;
    }

    @FXML
    private void handleSave() {
        try {
            // Get inputs
            LocalDate dateRep = dateRepField.getValue();
            String contenuRep = contenuRepField.getText().trim();

            // Validate inputs
            if (dateRep == null) {
                showAlert("Champ requis", "Veuillez sélectionner une date.", Alert.AlertType.WARNING);
                return;
            }
            if (dateRep.isBefore(LocalDate.now())) {
                showAlert("Erreur", "La date ne peut pas être dans le passé.", Alert.AlertType.WARNING);
                return;
            }
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
            response.setDateRep(dateRep);
            response.setContenuRep(contenuRep);

            // Save to database
            boolean success = responseService.addCareResponse(response, careId);
            if (success) {
                showAlert("Succès", "Réponse ajoutée avec succès.", Alert.AlertType.INFORMATION);
                handleCancel(); // Close window
            } else {
                showAlert("Erreur", "Échec de l'ajout de la réponse.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur Critique", "Une erreur inattendue s'est produite : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) dateRepField.getScene().getWindow();
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