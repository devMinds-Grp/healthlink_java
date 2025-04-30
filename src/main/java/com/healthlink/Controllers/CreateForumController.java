package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Services.ForumService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.util.Date;

public class CreateForumController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label titleErrorLabel;
    @FXML private Label descriptionErrorLabel;

    private MainController mainController;
    private final ForumService forumService = new ForumService();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Configuration des boutons
        cancelButton.setOnAction(event -> mainController.showForumList());
        submitButton.setOnAction(event -> handleSubmit());

        // Initialisation des labels d'erreur
        titleErrorLabel.setTextFill(Color.RED);
        descriptionErrorLabel.setTextFill(Color.RED);
        titleErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);

        // Validation en temps réel
        setupFieldValidations();
    }

    private void setupFieldValidations() {
        // Validation du titre (obligatoire + min 5 caractères)
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(titleErrorLabel, "Le titre est obligatoire");
            } else if (newVal.trim().length() < 5) {
                showFieldError(titleErrorLabel, "Minimum 5 caractères");
            } else {
                hideFieldError(titleErrorLabel);
            }
        });

        // Validation de la description (obligatoire + min 10 caractères)
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(descriptionErrorLabel, "La description est obligatoire");
            } else if (newVal.trim().length() < 10) {
                showFieldError(descriptionErrorLabel, "Minimum 10 caractères");
            } else {
                hideFieldError(descriptionErrorLabel);
            }
        });
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideFieldError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du titre
        if (titleField.getText().trim().isEmpty()) {
            showFieldError(titleErrorLabel, "Le titre est obligatoire");
            isValid = false;
        } else if (titleField.getText().trim().length() < 5) {
            showFieldError(titleErrorLabel, "Minimum 5 caractères");
            isValid = false;
        }

        // Validation de la description
        if (descriptionArea.getText().trim().isEmpty()) {
            showFieldError(descriptionErrorLabel, "La description est obligatoire");
            isValid = false;
        } else if (descriptionArea.getText().trim().length() < 10) {
            showFieldError(descriptionErrorLabel, "Minimum 10 caractères");
            isValid = false;
        }

        return isValid;
    }

    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }

        // Création du nouveau forum
        Forum forum = new Forum(
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                new Date(),
                1, // À remplacer par l'ID de l'utilisateur connecté
                false
        );

        // Sauvegarde en base
        forumService.add(forum);

        // Retour à la liste
        mainController.showForumList();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}