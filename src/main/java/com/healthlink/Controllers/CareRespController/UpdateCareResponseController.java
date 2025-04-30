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

public class UpdateCareResponseController {

    @FXML private DatePicker dateRepField;
    @FXML private TextArea contenuRepField;

    private CareResponse careResponse;
    private final CareResponseService responseService;

    public UpdateCareResponseController() {
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

    public void setCareResponse(CareResponse careResponse) {
        this.careResponse = careResponse;
        // Populate form with current CareResponse data
        LocalDate responseDate = careResponse.getDateRep();
        // Set to today if date is in the past
        if (responseDate.isBefore(LocalDate.now())) {
            dateRepField.setValue(LocalDate.now());
        } else {
            dateRepField.setValue(responseDate);
        }
        contenuRepField.setText(careResponse.getContenuRep());
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

            // Update CareResponse object
            careResponse.setDateRep(dateRep);
            careResponse.setContenuRep(contenuRep);

            // Save to database
            boolean success = responseService.updateCareResponse(careResponse);
            if (success) {
                showAlert("Succès", "Réponse mise à jour avec succès.", Alert.AlertType.INFORMATION);
                handleCancel(); // Close window
            } else {
                showAlert("Erreur", "Échec de la mise à jour de la réponse.", Alert.AlertType.ERROR);
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