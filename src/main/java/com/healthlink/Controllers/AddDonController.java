package com.healthlink.Controllers;

import com.healthlink.Entites.DonDuSang;
import com.healthlink.Services.DonDuSangService;
import com.healthlink.utils.MyDB;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AddDonController {

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField lieuField;

    @FXML
    private TextField dateField;

    @FXML
    private TextField numTelField;

    private DonDuSangService donDuSangService;

    @FXML
    public void initialize() {
        try {
            MyDB db = MyDB.getInstance();
            donDuSangService = new DonDuSangService(db.getConnection());
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (descriptionField == null || lieuField == null || dateField == null || numTelField == null) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "One or more fields failed to initialize. Check FXML and controller setup.");
            return;
        }

        if (donDuSangService == null) {
            showAlert(Alert.AlertType.ERROR, "Service Error", "Database service is not initialized. Please check the database connection.");
            return;
        }

        try {
            String description = descriptionField.getText().trim();
            String lieu = lieuField.getText().trim();
            String dateStr = dateField.getText().trim();
            String numTel = numTelField.getText().trim();

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

            // Validation for Lieu (Location)
            if (lieu.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Location cannot be empty.");
                return;
            }
            if (lieu.length() < 3) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Location must be at least 3 characters long.");
                return;
            }
            if (lieu.length() > 100) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Location cannot exceed 100 characters.");
                return;
            }
            if (!lieu.matches("[a-zA-Z\\s-]+")) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Location can only contain letters, spaces, and hyphens.");
                return;
            }

            // Validation for Date
            if (dateStr.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Date cannot be empty.");
                return;
            }
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date", "Date must be in YYYY-MM-DD format (e.g., 2025-04-15).");
                return;
            }
            LocalDate currentDate = LocalDate.now(); // Current date is April 15, 2025
            if (date.isBefore(currentDate)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date", "Date cannot be in the past. It must be on or after " + currentDate + ".");
                return;
            }

            // Validation for NumTel (Phone Number)
            if (numTel.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number cannot be empty.");
                return;
            }
            if (!numTel.matches("\\+?\\d+")) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number can only contain digits and an optional '+' at the start.");
                return;
            }
            if (numTel.length() < 8) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number must be at least 8 digits long.");
                return;
            }
            if (numTel.length() > 15) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number cannot exceed 15 digits.");
                return;
            }

            // If all validations pass, create and save the DonDuSang object
            DonDuSang don = new DonDuSang(description, lieu, date, numTel);

            if (donDuSangService.addDonDuSang(don)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Blood donation added successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add blood donation.");
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