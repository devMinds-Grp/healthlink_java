package com.healthlink.Controllers;

import com.healthlink.Entites.Categorie;
import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.CategorieService;
import com.healthlink.Services.ReclamationService;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class AddReclamationController {

    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private ComboBox<Categorie> categoryComboBox;
    @FXML private ImageView captchaImage;
    @FXML private TextField captchaInput;

    private String currentCaptchaText;
    private final ReclamationService reclamationService = new ReclamationService();
    private final CategorieService categorieService = new CategorieService();
    private final Random random = new Random();
    @FXML private TextField imagePathField;
    @FXML private Button browseImageBtn;
    @FXML private ImageView imagePreview;



    @FXML
    public void initialize() {
        loadCategories();
        setupFieldValidation();
        generateCaptcha();
        browseImageBtn.setOnAction(e -> handleBrowseImage());

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
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                titleField.setText(oldVal);
            }
        });

        descArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 500) {
                descArea.setText(oldVal);
            }
        });
    }
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(browseImageBtn.getScene().getWindow());
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getAbsolutePath());
            // Afficher un aperçu de l'image
            Image image = new Image(selectedFile.toURI().toString());
            imagePreview.setImage(image);
        }
    }
    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        if (!validateCaptcha()) {
            showAlert(Alert.AlertType.ERROR, "Erreur CAPTCHA", "Le texte CAPTCHA saisi est incorrect. Veuillez réessayer.");
            refreshCaptcha();
            return;
        }

        String titre = titleField.getText().trim();
        String description = descArea.getText().trim();
        Categorie selectedCategorie = categoryComboBox.getValue();

        Reclamation newRec = new Reclamation(titre, description, selectedCategorie.getId());
        newRec.setImage(imagePathField.getText()); // Ajouter le chemin de l'image

        try {
            if (reclamationService.addReclamation(newRec)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation ajoutée avec succès");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de la réclamation");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur technique", "Erreur: " + e.getMessage());
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

        if (captchaInput.getText().trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur", "Veuillez saisir le CAPTCHA");
            captchaInput.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateCaptcha() {
        String userInput = captchaInput.getText().trim();
        return userInput.equalsIgnoreCase(currentCaptchaText);
    }

    @FXML
    private void refreshCaptcha() {
        generateCaptcha();
        captchaInput.clear();
    }

    private void generateCaptcha() {
        // Générer un texte aléatoire pour le CAPTCHA
        currentCaptchaText = generateRandomString(6);

        // Créer une image CAPTCHA
        BufferedImage bufferedImage = new BufferedImage(150, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Remplir le fond
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 150, 50);

        // Dessiner le texte
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));

        // Ajouter des distorsions
        for (int i = 0; i < currentCaptchaText.length(); i++) {
            int x = 20 + i * 20;
            int y = 30 + random.nextInt(10) - 5;
            g2d.drawString(String.valueOf(currentCaptchaText.charAt(i)), x, y);
        }

        // Ajouter des lignes de bruit
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(150);
            int y1 = random.nextInt(50);
            int x2 = random.nextInt(150);
            int y2 = random.nextInt(50);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();

        // Convertir en image JavaFX
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            Image image = new Image(in);
            captchaImage.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de générer le CAPTCHA");
        }
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
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