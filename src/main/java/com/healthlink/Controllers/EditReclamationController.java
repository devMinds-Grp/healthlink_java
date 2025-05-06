package com.healthlink.Controllers;

import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.ReclamationService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class EditReclamationController {
    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private TextField imagePathField;
    @FXML private ImageView imagePreview;
    @FXML private Button browseImageBtn;
    private Reclamation reclamation;
    private ReclamationService service = new ReclamationService();

    public void setReclamationData(Reclamation r) {
        this.reclamation = r;
        titleField.setText(r.getTitre());
        descArea.setText(r.getDescription());

        // Afficher l'image existante si elle existe
        if (r.getImage() != null && !r.getImage().isEmpty()) {
            imagePathField.setText(r.getImage());
            try {
                File file = new File("src/main/resources/images/" + r.getImage());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imagePreview.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }

        setupFieldValidation();
    }
    @FXML
    public void initialize() {
        browseImageBtn.setOnAction(e -> handleBrowseImage());
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
    private void handleUpdate() {
        if (!validateInputs()) {
            return;
        }

        String newTitle = titleField.getText().trim();
        String newDesc = descArea.getText().trim();
        String imagePath = imagePathField.getText().trim();

        reclamation.setTitre(newTitle);
        reclamation.setDescription(newDesc);
        reclamation.setImage(imagePath); // Mettre à jour le chemin de l'image

        if (service.updateReclamation(reclamation)) {
            closeWindow();
        } else {
            showAlert("Erreur", "Échec de la mise à jour");
        }
    }

    private boolean validateInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le titre est obligatoire");
            titleField.requestFocus();
            return false;
        }

        if (titleField.getText().trim().length() < 5) {
            showAlert("Erreur", "Le titre doit contenir au moins 5 caractères");
            titleField.requestFocus();
            return false;
        }

        if (descArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "La description est obligatoire");
            descArea.requestFocus();
            return false;
        }

        if (descArea.getText().trim().length() < 20) {
            showAlert("Erreur", "La description doit contenir au moins 20 caractères");
            descArea.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        titleField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}