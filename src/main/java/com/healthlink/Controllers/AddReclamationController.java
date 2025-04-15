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
    @FXML private ComboBox<Categorie> categoryComboBox; // ComboBox pour les catégories

    private final ReclamationService reclamationService = new ReclamationService();
    private final CategorieService categorieService = new CategorieService();

    private List<Categorie> categories;

    @FXML
    public void initialize() {
        loadCategories();  // Charger les catégories au démarrage
    }

    private void loadCategories() {
        categories = categorieService.getAllCategories();  // Charger les catégories depuis le service
        if (categories.isEmpty()) {
            showAlert(AlertType.WARNING, "Aucune catégorie disponible", "Il n'y a aucune catégorie disponible pour ajouter une réclamation.");
            return;
        }

        // Remplir le ComboBox avec les catégories
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());  // Afficher le nom de la catégorie
                }
            }
        });

        categoryComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());  // Afficher le nom dans le bouton
                }
            }
        });

        // Sélectionner la première catégorie par défaut
        categoryComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSave() {
        String titre = titleField.getText().trim();
        String description = descArea.getText().trim();
        Categorie selectedCategorie = categoryComboBox.getValue();

        if (titre.isEmpty() || description.isEmpty() || selectedCategorie == null) {
            showAlert(AlertType.ERROR, "Champs incomplets", "Veuillez remplir tous les champs.");
        } else {
            Reclamation newRec = new Reclamation(titre, description, selectedCategorie.getId());
            try {
                if (reclamationService.addReclamation(newRec)) {
                    showAlert(AlertType.INFORMATION, "Réclamation ajoutée", "Votre réclamation a été ajoutée avec succès.");
                    closeWindow();
                } else {
                    showAlert(AlertType.ERROR, "Erreur", "Échec de l'ajout de la réclamation.");
                }
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur technique", "Une erreur technique est survenue : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    // Méthode pour afficher des alertes
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
