package com.healthlink.Controllers;

import com.healthlink.Entites.Reclamation;
import com.healthlink.utils.DeepLTranslationService;
import com.healthlink.utils.TranslationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Locale;

public class ViewReclamationController {

    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private TextArea originalDescArea;
    @FXML private TextArea translatedDescArea;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Label originalDescLabel;
    @FXML private Label translatedDescLabel;
    @FXML private Label languageLabel;
    @FXML private Button closeButton;

    private Reclamation reclamation;
    private final DeepLTranslationService translationService;

    public ViewReclamationController() {
        this.translationService = new DeepLTranslationService("b2c58875-8344-4581-bdf6-2204fa8ec37e:fx");
    }

    public void setReclamationData(Reclamation reclamation) {
        this.reclamation = reclamation;

        titleLabel.setText(reclamation.getTitre());
        categoryLabel.setText(reclamation.getCategorie().toString());
        originalDescArea.setText(reclamation.getDescription());

        languageComboBox.getItems().addAll("English", "French", "German", "Spanish", "Arabic");
        languageComboBox.getSelectionModel().select(0); // Default to English
        languageComboBox.setOnAction(event -> handleLanguageChange());
    }

    private void handleLanguageChange() {
        String selected = languageComboBox.getSelectionModel().getSelectedItem();
        String targetLang = getLanguageCode(selected);

        try {
            String translatedText = translationService.translate(reclamation.getDescription(), targetLang);
            translatedDescArea.setText(translatedText);
            translateUI(targetLang);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Translation Error", "Failed to translate: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleTranslate() {
        String selectedLanguage = languageComboBox.getSelectionModel().getSelectedItem();
        if (selectedLanguage == null || reclamation == null) return;

        String targetLang = getLanguageCode(selectedLanguage);

        try {
            String translatedText = translationService.translate(reclamation.getDescription(), targetLang);
            translatedDescArea.setText(translatedText);
        } catch (Exception e) {
            e.printStackTrace();
            translatedDescArea.setText("Translation error: " + e.getMessage());
        }
    }

    private void translateUI(String targetLang) {
        try {
            // Translate static UI elements
            languageLabel.setText(translationService.translate("Language:", targetLang));
            originalDescLabel.setText(translationService.translate("Original description:", targetLang));
            translatedDescLabel.setText(translationService.translate("Translated description:", targetLang));
            closeButton.setText(translationService.translate("Close", targetLang));

            // Translate dynamic content
            titleLabel.setText(translationService.translate(reclamation.getTitre(), targetLang));
            categoryLabel.setText(translationService.translate(reclamation.getCategorie().toString(), targetLang));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLanguageCode(String languageName) {
        switch (languageName) {
            case "French": return "FR";
            case "English": return "EN";
            case "Spanish": return "ES";
            case "German": return "DE";
            case "Arabic": return "AR";
            default: return "EN";
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}