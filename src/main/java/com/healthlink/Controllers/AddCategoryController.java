package com.healthlink.Controllers;

import com.healthlink.Entites.Categorie;
import com.healthlink.Services.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCategoryController {
    @FXML private TextField nameField;

    private CategorieService categorieService = new CategorieService();

    @FXML
    private void handleSave() {
        String nom = nameField.getText().trim();

        if (!nom.isEmpty()) {
            Categorie newCategorie = new Categorie(nom);
            if (categorieService.addCategorie(newCategorie)) {
                closeWindow();
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}