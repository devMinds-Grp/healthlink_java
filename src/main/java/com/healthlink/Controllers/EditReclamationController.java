package com.healthlink.Controllers;

import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.ReclamationService;
import javafx.fxml.FXML;
<<<<<<< HEAD
=======
import javafx.scene.control.Alert;
>>>>>>> master
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EditReclamationController {
    @FXML private TextField titleField;
    @FXML private TextArea descArea;

    private Reclamation reclamation;
    private ReclamationService service = new ReclamationService();

    public void setReclamationData(Reclamation r) {
        this.reclamation = r;
        titleField.setText(r.getTitre());
        descArea.setText(r.getDescription());
<<<<<<< HEAD
=======
        setupFieldValidation();
    }

    private void setupFieldValidation() {
        // Limiter la longueur du titre à 100 caractères
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                titleField.setText(oldVal);
            }
        });

        // Limiter la longueur de la description à 500 caractères
        descArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 500) {
                descArea.setText(oldVal);
            }
        });
>>>>>>> master
    }

    @FXML
    private void handleUpdate() {
<<<<<<< HEAD
        String newTitle = titleField.getText().trim();
        String newDesc = descArea.getText().trim();

        if (!newTitle.isEmpty() && !newDesc.isEmpty()) {
            reclamation.setTitre(newTitle);
            reclamation.setDescription(newDesc);
            if (service.updateReclamation(reclamation)) {
                closeWindow();
            }
        }
    }

=======
        if (!validateInputs()) {
            return;
        }

        String newTitle = titleField.getText().trim();
        String newDesc = descArea.getText().trim();

        reclamation.setTitre(newTitle);
        reclamation.setDescription(newDesc);

        if (service.updateReclamation(reclamation)) {
            closeWindow();
        } else {
            showAlert("Erreur", "Échec de la mise à jour");
        }
    }

    private boolean validateInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le titre est obligatoire");
            titleField.requestFocus();
            return false;
        }

        if (titleField.getText().trim().length() < 5) {
            showAlert("Erreur", "Le titre doit contenir au moins 5 caractères");
            titleField.requestFocus();
            return false;
        }

        if (descArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "La description est obligatoire");
            descArea.requestFocus();
            return false;
        }

        if (descArea.getText().trim().length() < 20) {
            showAlert("Erreur", "La description doit contenir au moins 20 caractères");
            descArea.requestFocus();
            return false;
        }

        return true;
    }

>>>>>>> master
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        titleField.getScene().getWindow().hide();
    }
<<<<<<< HEAD
=======

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
>>>>>>> master
}