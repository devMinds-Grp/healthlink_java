package com.healthlink.Controllers;

import com.healthlink.Entites.ReponseDon;
import com.healthlink.Services.ReponseDonService;
import com.healthlink.utils.MyDB;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AddReponseDonController {

    @FXML
    private TextArea descriptionField;

    private ReponseDonService reponseDonService;

    @FXML
    public void initialize() {
        try {
            MyDB db = MyDB.getInstance();
            reponseDonService = new ReponseDonService(db.getConnection());
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (descriptionField == null) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Description field failed to initialize. Check FXML and controller setup.");
            return;
        }

        if (reponseDonService == null) {
            showAlert(Alert.AlertType.ERROR, "Service Error", "Database service is not initialized. Please check the database connection.");
            return;
        }

        try {
            String description = descriptionField.getText().trim();

            // Validation for Description
            if (description.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Description cannot be empty.");
                return;
            }
            if (description.length() < 10) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Description must be at least 10 characters long.");
                return;
            }
            if (description.length() > 500) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Description cannot exceed 500 characters.");
                return;
            }

            ReponseDon reponse = new ReponseDon(description);

            if (reponseDonService.addReponseDon(reponse)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Response donation added successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add response donation.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) descriptionField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}