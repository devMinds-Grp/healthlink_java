package com.healthlink.Controllers;

import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.ReclamationService;
import javafx.fxml.FXML;
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
    }

    @FXML
    private void handleUpdate() {
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

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        titleField.getScene().getWindow().hide();
    }
}