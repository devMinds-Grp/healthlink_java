package com.healthlink.Controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.ReclamationService;
import com.healthlink.utils.TranslationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ListReclamationsControllerAdmin {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private Pagination pagination;

    private final ReclamationService service = new ReclamationService();
    private final TranslationService translationService = TranslationService.getInstance("votre_cle_api_deepl");
    private List<Reclamation> allReclamations;
    private static final int ITEMS_PER_PAGE = 6;

    @FXML
    public void initialize() {
        System.out.println("INITIALIZE lancé");
        loadAllReclamations();
        setupSearch();
    }

    private void loadAllReclamations() {
        allReclamations = service.getAllReclamations();
        int pageCount = (int) Math.ceil((double) allReclamations.size() / ITEMS_PER_PAGE);
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
        card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 10px;");
        card.setMinWidth(300);

        Label categoryLabel = new Label("Catégorie: " + r.getCategorie());
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        Label titleLabel = new Label(r.getTitre());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        // Status label with dynamic color based on status
        Label statusLabel = new Label("Statut: " + r.getStatut());
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        // Set color based on status
        switch (r.getStatut()) {
            case "Accepté":
                statusLabel.setStyle(statusLabel.getStyle() + " -fx-text-fill: #2ecc71;"); // Green
                break;
            case "Refusé":
                statusLabel.setStyle(statusLabel.getStyle() + " -fx-text-fill: #e74c3c;"); // Red
                break;
            case "En attente":
                statusLabel.setStyle(statusLabel.getStyle() + " -fx-text-fill: #f39c12;"); // Orange
                break;
        }

        // Image preview if exists
        ImageView imagePreview = createImagePreview(r);
        if (imagePreview != null) {
            imagePreview.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1px;");
            card.getChildren().add(imagePreview);
        }

        TextArea descArea = new TextArea(r.getDescription());
        descArea.setStyle("-fx-font-size: 14px; -fx-background-color: transparent; -fx-border-color: #dee2e6;");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);

        // QR Code
        try {
            ImageView qrCodeView = generateQRCode(r);
            qrCodeView.setFitWidth(100);
            qrCodeView.setFitHeight(100);
            qrCodeView.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1px;");
            card.getChildren().add(qrCodeView);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du QR Code: " + e.getMessage());
        }

        HBox adminButtonsBox = new HBox(10);
        adminButtonsBox.setStyle("-fx-alignment: center;");

        Button viewBtn = new Button("Consulter");
        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 12px;");

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px;");

        Button acceptBtn = new Button("Accepter");
        acceptBtn.setStyle("-fx-background-color: #00ff6e; -fx-text-fill: #ffffff; -fx-font-size: 12px;");

        Button rejectBtn = new Button("Refuser");
        rejectBtn.setStyle("-fx-background-color: #ff1900; -fx-text-fill: #fbfbfb; -fx-font-size: 12px;");

        viewBtn.setOnAction(e -> openViewDialog(r));
        editBtn.setOnAction(e -> openEditDialog(r));
        deleteBtn.setOnAction(e -> deleteReclamation(r));
        acceptBtn.setOnAction(e -> acceptReclamation(r));
        rejectBtn.setOnAction(e -> rejectReclamation(r));

        adminButtonsBox.getChildren().addAll(viewBtn, editBtn, deleteBtn, acceptBtn, rejectBtn);


        card.getChildren().addAll(categoryLabel, titleLabel, statusLabel, descArea, adminButtonsBox);

        return card;
    }    private ImageView generateQRCode(Reclamation reclamation) throws WriterException, IOException {
        String qrContent = String.format("Réclamation: %s\nCatégorie: %s\nDescription: %s",
                reclamation.getTitre(),
                reclamation.getCategoryId(),
                reclamation.getDescription());

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        Image qrImage = new Image(new ByteArrayInputStream(pngData));
        return new ImageView(qrImage);
    }

    private void acceptReclamation(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Accepter la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir accepter la réclamation '" + r.getTitre() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && service.acceptReclamation(r.getId())) {
                loadAllReclamations(); // Rafraîchir la liste
            }
        });
    }

    private void rejectReclamation(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Refuser la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir refuser la réclamation '" + r.getTitre() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && service.rejectReclamation(r.getId())) {
                loadAllReclamations(); // Rafraîchir la liste
            }
        });
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
    private ImageView createImagePreview(Reclamation r) {
        if (r.getImage() != null && !r.getImage().isEmpty()) {
            try {
                // Construire le chemin complet vers l'image dans le dossier resources
                String imagePath = "src/main/resources/images/" + r.getImage();
                File file = new File(imagePath);

                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(150);
                    imageView.setFitHeight(150);
                    imageView.setPreserveRatio(true);
                    imageView.getStyleClass().add("reclamation-image");
                    return imageView;
                } else {
                    System.err.println("Fichier image introuvable: " + imagePath);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
        return null;
    }    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            allReclamations = service.getAllReclamations().stream()
                    .filter(r -> r.getTitre().toLowerCase().contains(newVal.toLowerCase()) ||
                            r.getDescription().toLowerCase().contains(newVal.toLowerCase()) ).toList();

            int pageCount = Math.max(1, (int) Math.ceil((double) allReclamations.size() / ITEMS_PER_PAGE));
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::createPage);
        });
    }
}