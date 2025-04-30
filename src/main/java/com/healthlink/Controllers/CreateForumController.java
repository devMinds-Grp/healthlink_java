package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Services.ForumService;
import com.healthlink.utils.InappropriateWordsFilter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateForumController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label titleErrorLabel;
    @FXML private Label descriptionErrorLabel;

    private MainController mainController;
    private final ForumService forumService = new ForumService();
    private int currentUserId;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
        System.out.println("CreateForumController - currentUserId défini à : " + currentUserId);
    }

    @FXML
    public void initialize() {
        cancelButton.setOnAction(event -> mainController.showForumList());
        submitButton.setOnAction(event -> handleSubmit());

        titleErrorLabel.setTextFill(Color.RED);
        descriptionErrorLabel.setTextFill(Color.RED);
        titleErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);

        setupFieldValidations();
    }

    private void setupFieldValidations() {
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Validation titre: " + newVal);
            List<String> errors = new ArrayList<>();
            if (newVal.trim().isEmpty()) {
                errors.add("Le titre est obligatoire");
            } else if (newVal.trim().length() < 5) {
                errors.add("Minimum 5 caractères");
            }
            if (InappropriateWordsFilter.containsInappropriateWords(newVal)) {
                errors.add("Le titre contient des mots inappropriés");
            }
            if (errors.isEmpty()) {
                hideFieldError(titleErrorLabel);
            } else {
                showFieldError(titleErrorLabel, String.join("\n", errors));
            }
        });

        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Validation description: " + newVal);
            List<String> errors = new ArrayList<>();
            if (newVal.trim().isEmpty()) {
                errors.add("La description est obligatoire");
            } else if (newVal.trim().length() < 10) {
                errors.add("Minimum 10 caractères");
            }
            if (InappropriateWordsFilter.containsInappropriateWords(newVal)) {
                errors.add("La description contient des mots inappropriés");
            }
            if (errors.isEmpty()) {
                hideFieldError(descriptionErrorLabel);
            } else {
                showFieldError(descriptionErrorLabel, String.join("\n", errors));
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
        List<String> inappropriateWordAlerts = new ArrayList<>();

        // Validation du titre
        String titleText = titleField.getText();
        System.out.println("Validation finale titre: " + titleText);
        List<String> titleErrors = new ArrayList<>();
        if (titleText.trim().isEmpty()) {
            titleErrors.add("Le titre est obligatoire");
            isValid = false;
        } else if (titleText.trim().length() < 5) {
            titleErrors.add("Minimum 5 caractères");
            isValid = false;
        }
        if (InappropriateWordsFilter.containsInappropriateWords(titleText)) {
            titleErrors.add("Le titre contient des mots inappropriés");
            String inappropriateWord = InappropriateWordsFilter.getFirstInappropriateWord(titleText);
            inappropriateWordAlerts.add("Le titre contient le mot inapproprié : " + inappropriateWord);
            isValid = false;
        }
        if (!titleErrors.isEmpty()) {
            showFieldError(titleErrorLabel, String.join("\n", titleErrors));
        } else {
            hideFieldError(titleErrorLabel);
        }

        // Validation de la description
        String descriptionText = descriptionArea.getText();
        System.out.println("Validation finale description: " + descriptionText);
        List<String> descriptionErrors = new ArrayList<>();
        if (descriptionText.trim().isEmpty()) {
            descriptionErrors.add("La description est obligatoire");
            isValid = false;
        } else if (descriptionText.trim().length() < 10) {
            descriptionErrors.add("Minimum 10 caractères");
            isValid = false;
        }
        if (InappropriateWordsFilter.containsInappropriateWords(descriptionText)) {
            descriptionErrors.add("La description contient des mots inappropriés");
            String inappropriateWord = InappropriateWordsFilter.getFirstInappropriateWord(descriptionText);
            inappropriateWordAlerts.add("La description contient le mot inapproprié : " + inappropriateWord);
            isValid = false;
        }
        if (!descriptionErrors.isEmpty()) {
            showFieldError(descriptionErrorLabel, String.join("\n", descriptionErrors));
        } else {
            hideFieldError(descriptionErrorLabel);
        }

        // Afficher une seule alerte pour tous les mots inappropriés
        if (!inappropriateWordAlerts.isEmpty()) {
            showAlert("Contenu inapproprié", String.join("\n", inappropriateWordAlerts));
        }

        return isValid;
    }

    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }

        if (currentUserId == -1) {
            showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter pour créer un forum.");
            return;
        }

        Forum forum = new Forum(
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                new Date(),
                currentUserId,
                false
        );

        try {
            forumService.add(forum);
            showAlert("Succès", "Forum créé avec succès !");
        } catch (Exception e) {
            showAlert("Erreur", "Échec de la création du forum : " + e.getMessage());
            e.printStackTrace();
            return;
        }

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