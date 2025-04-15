package com.healthlink.Controllers;


import com.healthlink.Entites.Categorie;
import com.healthlink.Services.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EditCategoryController {
    @FXML private TextField nameField;

    private Categorie categorie;
    private CategorieService categorieService = new CategorieService();

    public void setCategoryData(Categorie categorie) {
        this.categorie = categorie;
        nameField.setText(categorie.getNom());
    }

    @FXML
    private void handleUpdate() {
        String newName = nameField.getText().trim();

        if (!newName.isEmpty() && !newName.equals(categorie.getNom())) {
            categorie.setNom(newName);
            if (categorieService.updateCategorie(categorie)) {
                closeWindow();
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        nameField.getScene().getWindow().hide();
    }
}