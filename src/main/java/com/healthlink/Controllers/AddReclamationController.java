package com.healthlink.Controllers;

import com.healthlink.Entites.Categorie;
import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.CategorieService;
import com.healthlink.Services.ReclamationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class AddReclamationController {

    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private ComboBox<Categorie> categoryComboBox;

    private final ReclamationService reclamationService = new ReclamationService();
    private final CategorieService categorieService = new CategorieService();

    @FXML
    public void initialize() {
        loadCategories();
        setupFieldValidation();
    }

    private void loadCategories() {
        List<Categorie> categories = categorieService.getAllCategories();
        if (categories.isEmpty()) {
            showAlert(AlertType.WARNING, "Aucune catégorie disponible",
                    "Il n'y a aucune catégorie disponible pour ajouter une réclamation.");
            return;
        }

        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });

        categoryComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });

        categoryComboBox.getSelectionModel().selectFirst();
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
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        String titre = titleField.getText().trim();
        String description = descArea.getText().trim();
        Categorie selectedCategorie = categoryComboBox.getValue();

        Reclamation newRec = new Reclamation(titre, description, selectedCategorie.getId());
        try {
            if (reclamationService.addReclamation(newRec)) {
                showAlert(AlertType.INFORMATION, "Succès", "Réclamation ajoutée avec succès");
                closeWindow();
            } else {
                showAlert(AlertType.ERROR, "Erreur", "Échec de l'ajout de la réclamation");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur technique", "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur", "Le titre est obligatoire");
            titleField.requestFocus();
            return false;
        }

        if (titleField.getText().trim().length() < 5) {
            showAlert(AlertType.ERROR, "Erreur", "Le titre doit contenir au moins 5 caractères");
            titleField.requestFocus();
            return false;
        }

        if (descArea.getText().trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur", "La description est obligatoire");
            descArea.requestFocus();
            return false;
        }

        if (descArea.getText().trim().length() < 20) {
            showAlert(AlertType.ERROR, "Erreur", "La description doit contenir au moins 20 caractères");
            descArea.requestFocus();
            return false;
        }

        if (categoryComboBox.getValue() == null) {
            showAlert(AlertType.ERROR, "Erreur", "Veuillez sélectionner une catégorie");
            categoryComboBox.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}