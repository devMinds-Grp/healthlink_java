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

public class AddCareController {

    @FXML private DatePicker dateField;
    @FXML private TextField addressField;
    @FXML private TextArea descriptionField;

    private final CareService careService;

    public AddCareController() {
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

        // Create Care object
        Care care = new Care();
        care.setDate(date);
        care.setAddress(address);
        care.setDescription(description);

        // Save to database
        try {
            boolean success = careService.addCare(care);
            if (success) {
                showAlert("Succès", "Soin ajouté avec succès.", Alert.AlertType.INFORMATION);
                handleCancel(); // Close window
            } else {
                showAlert("Erreur", "Échec de l'ajout du soin.", Alert.AlertType.ERROR);
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