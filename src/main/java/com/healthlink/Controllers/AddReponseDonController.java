package com.healthlink.Controllers;

import com.healthlink.Entites.ReponseDon;
import com.healthlink.Services.ReponseDonService;
import com.healthlink.utils.MyDB;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class AddReponseDonController {

    @FXML
    private TextArea descriptionField;

    private ReponseDonService reponseDonService;
    private int bloodDonationId; // Changed from donDuSangId

    @FXML
    public void initialize() {
        try {
            Connection connection = MyDB.getInstance().getConnection();
            reponseDonService = new ReponseDonService(connection);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database: " + e.getMessage());
        }
    }

    public void setBloodDonationId(int id) { // Changed from setDonDuSangId
        this.bloodDonationId = id;
    }

    @FXML
    private void handleSave() {
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Description cannot be empty.");
            return;
        }

        ReponseDon reponse = new ReponseDon(description, bloodDonationId);
        if (reponseDonService.addReponseDon(reponse)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Response added successfully!");
            Stage stage = (Stage) descriptionField.getScene().getWindow();
            stage.close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add response to the database.");
        }
    }

    @FXML
    private void handleCancel() {
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