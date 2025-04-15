package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Services.ForumService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class EditForumController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private Label titleErrorLabel;
    @FXML private Label descriptionErrorLabel;

    private int currentUserId = 1;
    private Forum forum;
    private MainController mainController;
    private final ForumService forumService = new ForumService();

    public void setForum(Forum forum, int currentUserId) {
        this.forum = forum;
        this.currentUserId = currentUserId;
        titleField.setText(forum.getTitle());
        descriptionArea.setText(forum.getDescription());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
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

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        // Mise à jour de l'objet Forum
        forum.setTitle(titleField.getText().trim());
        forum.setDescription(descriptionArea.getText().trim());

        // Sauvegarde en base de données
        forumService.update(forum);

        // Retour à la liste des forums
        mainController.showForumList();
    }

    @FXML
    private void handleCancel() {
        // Retour à la liste sans sauvegarder
        mainController.showForumList();
    }
}