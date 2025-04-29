package com.healthlink.Controllers;

import com.healthlink.Entities.ForumResponse;
import com.healthlink.Services.ForumResponseService;
import com.healthlink.utils.InappropriateWordsFilter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class EditCommentController {
    @FXML private TextArea commentArea;
    @FXML private Label commentErrorLabel;

    private ForumResponse response;
    private MainController mainController;
    private final ForumResponseService responseService = new ForumResponseService();

    public void setResponse(ForumResponse response) {
        this.response = response;
        commentArea.setText(response.getDescription());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        commentErrorLabel.setTextFill(Color.RED);
        commentErrorLabel.setVisible(false);

        setupFieldValidations();
    }

    private void setupFieldValidations() {
        commentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Validation commentaire: " + newVal);
            List<String> errors = new ArrayList<>();
            if (newVal.trim().isEmpty()) {
                errors.add("Le commentaire est obligatoire");
            } else if (newVal.trim().length() < 10) {
                errors.add("Minimum 10 caractères");
            }
            if (InappropriateWordsFilter.containsInappropriateWords(newVal)) {
                errors.add("Le commentaire contient des mots inappropriés");
            }
            if (errors.isEmpty()) {
                hideFieldError(commentErrorLabel);
            } else {
                showFieldError(commentErrorLabel, String.join("\n", errors));
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

        // Validation du commentaire
        String commentText = commentArea.getText();
        System.out.println("Validation finale commentaire: " + commentText);
        List<String> commentErrors = new ArrayList<>();
        if (commentText.trim().isEmpty()) {
            commentErrors.add("Le commentaire est obligatoire");
            isValid = false;
        } else if (commentText.trim().length() < 10) {
            commentErrors.add("Minimum 10 caractères");
            isValid = false;
        }
        if (InappropriateWordsFilter.containsInappropriateWords(commentText)) {
            commentErrors.add("Le commentaire contient des mots inappropriés");
            String inappropriateWord = InappropriateWordsFilter.getFirstInappropriateWord(commentText);
            inappropriateWordAlerts.add("Le commentaire contient le mot inapproprié : " + inappropriateWord);
            isValid = false;
        }
        if (!commentErrors.isEmpty()) {
            showFieldError(commentErrorLabel, String.join("\n", commentErrors));
        } else {
            hideFieldError(commentErrorLabel);
        }

        // Afficher une alerte pour les mots inappropriés
        if (!inappropriateWordAlerts.isEmpty()) {
            showAlert("Contenu inapproprié", String.join("\n", inappropriateWordAlerts));
        }

        return isValid;
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        response.setDescription(commentArea.getText().trim());
        responseService.update(response);
        mainController.showForumDetails(mainController.getCurrentForum());
    }

    @FXML
    private void handleCancel() {
        mainController.showForumDetails(mainController.getCurrentForum());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}