package com.healthlink.Controllers;

import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.ReclamationService;
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

public class ListReclamationsController {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;

    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {
        System.out.println("INITIALIZE lancé");
        loadReclamations();
        setupSearch();
    }

    private void loadReclamations() {
        cardsContainer.getChildren().clear();
        List<Reclamation> reclamations = service.getAllReclamations();
        System.out.println("Nombre de réclamations récupérées : " + reclamations.size());

        for (Reclamation r : reclamations) {
            VBox card = createReclamationCard(r);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createReclamationCard(Reclamation r) {
        VBox card = new VBox(10);
        card.getStyleClass().add("reclamation-card");
        card.setMinWidth(300);

        Label categoryLabel = new Label("Catégorie: " + r.getCategorie());
        categoryLabel.getStyleClass().add("reclamation-category");

        Label titleLabel = new Label(r.getTitre());
        titleLabel.getStyleClass().add("reclamation-title");

        TextArea descArea = new TextArea(r.getDescription());
        descArea.getStyleClass().add("reclamation-desc");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);

        HBox buttonsBox = new HBox(10);
        Button editBtn = new Button("Modifier");
        Button deleteBtn = new Button("Supprimer");

        editBtn.getStyleClass().add("edit-btn");
        deleteBtn.getStyleClass().add("delete-btn");

        editBtn.setOnAction(e -> openEditDialog(r));
        deleteBtn.setOnAction(e -> deleteReclamation(r));

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(categoryLabel, titleLabel, descArea, buttonsBox);

        return card;
    }

    private void openEditDialog(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_reclamation.fxml"));
            Parent root = loader.load();

            EditReclamationController controller = loader.getController();
            controller.setReclamationData(r);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadReclamations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteReclamation(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + r.getTitre() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && service.deleteReclamation(r.getId())) {
                loadReclamations();
            }
        });
    }

    @FXML
    private void handleAddReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_reclamation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadReclamations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            cardsContainer.getChildren().clear();
            service.getAllReclamations().stream()
                    .filter(r -> r.getTitre().toLowerCase().contains(newVal.toLowerCase()) ||
                            r.getDescription().toLowerCase().contains(newVal.toLowerCase()) ||
                            r.getCategorie().getNom().toLowerCase().contains(newVal.toLowerCase()))
                    .forEach(r -> cardsContainer.getChildren().add(createReclamationCard(r)));
        });
    }
}