package com.healthlink.Controllers.CareController;

import com.healthlink.Entities.Care;
import com.healthlink.Services.CareService;
import com.healthlink.utils.MyDB;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDate;

public class UpdateCareController {

    @FXML private DatePicker dateField;
    @FXML private TextField addressField;
    @FXML private TextArea descriptionField;

    private Care care;
    private final CareService careService;

    public UpdateCareController() {
        try {
            this.careService = new CareService(MyDB.getInstance().getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize CareService", e);
        }
    }

    @FXML
    public void initialize() {
        // Restrict DatePicker to today or future dates
        dateField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    public void setCare(Care care) {
        this.care = care;
        // Populate form with current Care data
        LocalDate careDate = care.getDate();
        // Ensure date is not in the past; if it is, set to today
        if (careDate.isBefore(LocalDate.now())) {
            dateField.setValue(LocalDate.now());
        } else {
            dateField.setValue(careDate);
        }
        addressField.setText(care.getAddress());
        descriptionField.setText(care.getDescription());
    }

    @FXML
    private void handleSave() {
        // Get inputs
        LocalDate date = dateField.getValue();
        String address = addressField.getText().trim();
        String description = descriptionField.getText().trim();

        // Validate inputs
        if (date == null) {
            showAlert("Champ requis", "Veuillez sélectionner une date.", Alert.AlertType.WARNING);
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date ne peut pas être dans le passé.", Alert.AlertType.WARNING);
            return;
        }
        if (address.isEmpty()) {
            showAlert("Champ requis", "Veuillez entrer une adresse.", Alert.AlertType.WARNING);
            return;
        }
        if (address.length() < 5) {
            showAlert("Erreur", "L'adresse doit contenir au moins 5 caractères.", Alert.AlertType.WARNING);
            return;
        }
        if (description.isEmpty()) {
            showAlert("Champ requis", "Veuillez entrer une description.", Alert.AlertType.WARNING);
            return;
        }
        if (description.length() < 10) {
            showAlert("Erreur", "La description doit contenir au moins 10 caractères.", Alert.AlertType.WARNING);
            return;
        }

        // Update Care object
        care.setDate(date);
        care.setAddress(address);
        care.setDescription(description);

        // Save to database
        try {
            boolean success = careService.updateCare(care);
            if (success) {
                showAlert("Succès", "Soin mis à jour avec succès.", Alert.AlertType.INFORMATION);
                handleCancel(); // Close window
            } else {
                showAlert("Erreur", "Échec de la mise à jour du soin.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur Critique", "Une erreur inattendue s'est produite : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) dateField.getScene().getWindow();
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