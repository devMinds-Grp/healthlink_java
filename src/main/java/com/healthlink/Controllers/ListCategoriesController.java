package com.healthlink.Controllers;

import com.healthlink.Entites.Categorie;
import com.healthlink.Services.CategorieService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ListCategoriesController {
    @FXML private FlowPane categoriesContainer;
    @FXML private TextField searchField;

    private CategorieService categorieService = new CategorieService();

    @FXML
    public void initialize() {
        loadCategories();
        setupSearch();
    }

    private void loadCategories() {
        categoriesContainer.getChildren().clear();
        List<Categorie> categories = categorieService.getAllCategories();

        for (Categorie categorie : categories) {
            VBox card = createCategoryCard(categorie);
            categoriesContainer.getChildren().add(card);
        }
    }

    private VBox createCategoryCard(Categorie categorie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("category-card");

        Label nameLabel = new Label(categorie.getNom());
        nameLabel.getStyleClass().add("category-name");

        HBox buttonsBox = new HBox(10);
        Button editBtn = new Button("Modifier");
        Button deleteBtn = new Button("Supprimer");

        editBtn.getStyleClass().add("edit-btn");
        deleteBtn.getStyleClass().add("delete-btn");

        editBtn.setOnAction(e -> openEditDialog(categorie));
        deleteBtn.setOnAction(e -> deleteCategory(categorie));

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(nameLabel, buttonsBox);

        return card;
    }

    private void openEditDialog(Categorie categorie) {
        try {
            // Corriger le chemin du fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_category.fxml"));
            Parent root = loader.load();

            EditCategoryController controller = loader.getController();
            controller.setCategoryData(categorie);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadCategories(); // Rafraîchir après modification
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteCategory(Categorie categorie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + categorie.getNom() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (categorieService.deleteCategorie(categorie.getId())) {
                    loadCategories();
                }
            }
        });
    }

    @FXML
    private void handleAddCategory() {
        try {
            // Corriger le chemin du fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_category.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadCategories(); // Rafraîchir après ajout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            categoriesContainer.getChildren().clear();
            categorieService.getAllCategories().stream()
                    .filter(c -> c.getNom().toLowerCase().contains(newVal.toLowerCase()))
                    .forEach(c -> categoriesContainer.getChildren().add(createCategoryCard(c)));
        });
    }
}
