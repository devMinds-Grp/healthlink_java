package com.healthlink.Controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.ReclamationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ListReclamationsController {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private Pagination pagination;

    private final ReclamationService service = new ReclamationService();
    private List<Reclamation> allReclamations;
    private static final int ITEMS_PER_PAGE = 6;

    @FXML
    public void initialize() {
        loadAllReclamations();
        setupSearch();
    }

    private void loadAllReclamations() {
        allReclamations = service.getAllReclamations();
        int pageCount = Math.max(1, (int) Math.ceil((double) allReclamations.size() / ITEMS_PER_PAGE));
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allReclamations.size());

        FlowPane pageFlowPane = new FlowPane();
        pageFlowPane.setHgap(20);
        pageFlowPane.setVgap(20);
        pageFlowPane.setPrefWrapLength(960);

        for (int i = fromIndex; i < toIndex; i++) {
            Reclamation r = allReclamations.get(i);
            VBox card = createReclamationCard(r);
            pageFlowPane.getChildren().add(card);
        }

        VBox pageBox = new VBox(pageFlowPane);
        pageBox.setFillWidth(true);
        return pageBox;
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

        try {
            ImageView qrCodeView = generateQRCode(r);
            qrCodeView.setFitWidth(100);
            qrCodeView.setFitHeight(100);
            card.getChildren().add(qrCodeView);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du QR Code: " + e.getMessage());
        }

        HBox buttonsBox = new HBox(10);
        Button viewBtn = new Button("Consulter");
        Button editBtn = new Button("Modifier");
        Button deleteBtn = new Button("Supprimer");

        viewBtn.getStyleClass().add("view-btn");
        editBtn.getStyleClass().add("edit-btn");
        deleteBtn.getStyleClass().add("delete-btn");

        viewBtn.setOnAction(e -> openViewDialog(r));
        editBtn.setOnAction(e -> openEditDialog(r));
        deleteBtn.setOnAction(e -> deleteReclamation(r));

        buttonsBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        card.getChildren().addAll(categoryLabel, titleLabel, descArea, buttonsBox);

        return card;
    }

    private ImageView generateQRCode(Reclamation reclamation) throws WriterException, IOException {
        String qrContent = String.format("Réclamation: %s\nCatégorie: %s\nDescription: %s",
                reclamation.getTitre(),
                reclamation.getCategorie(),
                reclamation.getDescription());

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        Image qrImage = new Image(new ByteArrayInputStream(pngData));
        return new ImageView(qrImage);
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

            loadAllReclamations();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la fenêtre d'édition: " + e.getMessage());
        }
    }

    private void openViewDialog(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view_reclamation.fxml"));
            Parent root = loader.load();

            ViewReclamationController controller = loader.getController();
            controller.setReclamationData(reclamation);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de la réclamation");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la fenêtre de visualisation: " + e.getMessage());
        }
    }

    private void deleteReclamation(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + r.getTitre() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && service.deleteReclamation(r.getId())) {
                loadAllReclamations();
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

            loadAllReclamations();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la fenêtre d'ajout: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            allReclamations = service.getAllReclamations().stream()
                    .filter(r -> r.getTitre().toLowerCase().contains(newVal.toLowerCase()) ||
                            r.getDescription().toLowerCase().contains(newVal.toLowerCase()) ||
                            r.getCategorie().getNom().toLowerCase().contains(newVal.toLowerCase()))
                    .toList();

            int pageCount = Math.max(1, (int) Math.ceil((double) allReclamations.size() / ITEMS_PER_PAGE));
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::createPage);
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}