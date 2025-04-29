package com.healthlink.Controllers.Prescription;

import com.healthlink.Entities.Prescription;
import com.healthlink.Services.PrescriptionService;
import com.healthlink.Services.AuthService;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.EmailService;
import com.healthlink.Services.UserService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ListPrescriptionController {

    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TextField searchField;
    @FXML private Button sortAscButton;
    @FXML private Button sortDescButton;
    @FXML private TableColumn<Prescription, String> nomMedicamentColumn;
    @FXML private TableColumn<Prescription, String> dosageColumn;
    @FXML private TableColumn<Prescription, String> dureeColumn;
    @FXML private TableColumn<Prescription, String> notesColumn;
    @FXML private TableColumn<Prescription, String> dateColumn;
    @FXML private TableColumn<Prescription, String> patientNameColumn;
    @FXML private TableColumn<Prescription, Void> actionsColumn;

    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private ObservableList<Prescription> prescriptionList;
    private FilteredList<Prescription> filteredPrescriptions;

    // Define colors from HTML
    private static final DeviceRgb BACKGROUND_COLOR = new DeviceRgb(240, 244, 248); // #f0f4f8
    private static final DeviceRgb HEADER_FOOTER_COLOR = new DeviceRgb(74, 144, 226); // #4a90e2
    private static final DeviceRgb TEXT_GRAY = new DeviceRgb(85, 85, 85); // #555
    private static final DeviceRgb EVEN_ROW_COLOR = new DeviceRgb(249, 249, 249); // #f9f9f9
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(224, 224, 224); // #e0e0e0

    @FXML
    public void initialize() {
        try {
            Utilisateur user = AuthService.getConnectedUtilisateur();
            System.out.println("Utilisateur connecté : " + user);
            if (user == null || user.getRole().getId() != 2) { // Assuming role 2 is for doctors
                showAlert("Erreur", "Utilisateur non autorisé ou non connecté.", Alert.AlertType.ERROR);
                return;
            }

            setupTableColumns();
            loadPrescriptions();
            setupSearchFilter();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'initialisation : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadPrescriptions() {
        try {
            List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
            if (prescriptions == null || prescriptions.isEmpty()) {
                showAlert("Information", "Aucune prescription trouvée.", Alert.AlertType.INFORMATION);
                prescriptionList = FXCollections.observableArrayList();
            } else {
                prescriptionList = FXCollections.observableArrayList(prescriptions);
            }
            if (prescriptionList != null) {
                setupSearchFilter();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des prescriptions : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupSearchFilter() {
        if (searchField == null) {
            System.err.println("searchField is null, cannot set up search filter.");
            return;
        }
        filteredPrescriptions = new FilteredList<>(prescriptionList != null ? prescriptionList : FXCollections.observableArrayList(), p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredPrescriptions.setPredicate(prescription -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return (prescription.getPatientName() != null && prescription.getPatientName().toLowerCase().contains(lowerCaseFilter)) ||
                            (prescription.getNomMedicament() != null && prescription.getNomMedicament().toLowerCase().contains(lowerCaseFilter));
                })
        );

        SortedList<Prescription> sortedPrescriptions = new SortedList<>(filteredPrescriptions);
        sortedPrescriptions.comparatorProperty().bind(prescriptionsTable.comparatorProperty());
        prescriptionsTable.setItems(sortedPrescriptions);
    }

    private void setupTableColumns() {
        nomMedicamentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomMedicament()));
        dosageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDosage()));
        dureeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDuree()));
        notesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNotes()));
        dateColumn.setCellValueFactory(cellData -> {
            String appointmentDate = cellData.getValue().getAppointmentDate();
            return new SimpleStringProperty(appointmentDate != null ? appointmentDate : "Non défini");
        });
        patientNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        patientNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Utilisateur inconnu")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final Button sendBtn = new Button("Envoyer");
            private final HBox buttonsBox = new HBox(10, editBtn, deleteBtn, sendBtn);

            {
                editBtn.getStyleClass().add("edit-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                sendBtn.getStyleClass().add("send-btn");

                editBtn.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    showUpdateForm(prescription);
                });

                deleteBtn.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    deletePrescription(prescription);
                });

                sendBtn.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    sendPrescriptionEmail(prescription);
                });

                buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });
    }

    @FXML
    public void showCreateForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Prescription/create_prescription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) prescriptionsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer une Prescription");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le formulaire de création : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showUpdateForm(Prescription prescription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Prescription/update_prescription.fxml"));
            Parent root = loader.load();
            UpdatePrescriptionController controller = loader.getController();
            controller.setPrescription(prescription);
            controller.setListPrescriptionController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier une Prescription");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void refreshPrescriptions() {
        loadPrescriptions();
    }

    private void deletePrescription(Prescription prescription) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la prescription");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette prescription ?");

        // Apply custom styling to the confirmation popup
        DialogPane dialogPane = alert.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/css/Prescription.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-alert");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS pour la popup de confirmation : " + e.getMessage());
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (prescriptionService.deletePrescription(prescription.getId())) {
                    loadPrescriptions();
                    showAlert("Succès", "La prescription a été supprimée avec succès.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Échec de la suppression de la prescription.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void sendPrescriptionEmail(Prescription prescription) {
        try {
            String patientEmail = getPatientEmail(prescription);
            if (patientEmail == null) {
                showAlert("Erreur", "Impossible de trouver l'email du patient.", Alert.AlertType.ERROR);
                return;
            }

            byte[] pdfBytes = generatePrescriptionPDF(prescription);
            if (pdfBytes == null) {
                showAlert("Erreur", "Échec de la génération du PDF.", Alert.AlertType.ERROR);
                return;
            }

            emailService.sendPrescriptionEmail(patientEmail, prescription.getPatientName().split(" ")[0],
                    prescription.getPatientName().split(" ").length > 1 ? prescription.getPatientName().split(" ")[1] : "",
                    pdfBytes);
            showAlert("Succès", "La prescription a été envoyée avec succès à " + patientEmail + ".", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Échec de l'envoi de la prescription : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private String getPatientEmail(Prescription prescription) {
        String sql = "SELECT u.email " +
                "FROM appointment a " +
                "JOIN utilisateur u ON a.patient_id = u.id " +
                "WHERE a.id = ?";
        try (java.sql.Connection conn = com.healthlink.utils.MyDB.getInstance().getConnection();
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, prescription.getRdvCardId());
            try (java.sql.ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Erreur lors de la récupération de l'email du patient : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private byte[] generatePrescriptionPDF(Prescription prescription) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // Set page background color
            PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
            canvas.setFillColor(BACKGROUND_COLOR);
            canvas.rectangle(0, 0, PageSize.A4.getWidth(), PageSize.A4.getHeight());
            canvas.fill();

            // Container (white background with shadow)
            float containerWidth = 500;
            float containerMargin = (PageSize.A4.getWidth() - containerWidth) / 2;
            document.setMargins(50, containerMargin, 50, containerMargin);

            // Header (blue background)
            Table headerTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            headerTable.setBackgroundColor(HEADER_FOOTER_COLOR);
            headerTable.setBorder(new SolidBorder(BORDER_COLOR, 1));
            Cell headerCell = new Cell()
                    .add(new Paragraph("Prescription Médicale")
                            .setFontSize(24)
                            .setBold()
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setPadding(20)
                    .setBorder(Border.NO_BORDER);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            // Greeting Message
            String patientFirstName = prescription.getPatientName().split(" ")[0];
            document.add(new Paragraph("Bonjour " + patientFirstName + ",")
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));
            document.add(new Paragraph("Nous espérons que vous allez mieux. Voici votre prescription médicale.")
                    .setFontSize(16)
                    .setFontColor(TEXT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(15));
            document.add(new Paragraph("Veuillez prendre soin de vous et suivre les recommandations prescrites par votre médecin.")
                    .setFontSize(16)
                    .setFontColor(TEXT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(15));

            // Prescription Details Table
            Table prescriptionTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
            prescriptionTable.setMarginTop(22);
            prescriptionTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

            // Table Headers
            String[] headers = {"Nom du patient", "Nom du médicament", "Dosage", "Durée", "Notes", "Date du RDV"};
            String[] values = {
                    prescription.getPatientName(),
                    prescription.getNomMedicament(),
                    prescription.getDosage(),
                    prescription.getDuree(),
                    prescription.getNotes() != null ? prescription.getNotes() : "Aucune",
                    prescription.getAppointmentDate() != null ? prescription.getAppointmentDate() : "Non défini"
            };

            for (int i = 0; i < headers.length; i++) {
                // Header cell
                prescriptionTable.addCell(new Cell()
                        .add(new Paragraph(headers[i])
                                .setFontSize(12)
                                .setBold()
                                .setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(HEADER_FOOTER_COLOR)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(10)
                        .setBorder(new SolidBorder(BORDER_COLOR, 1)));

                // Value cell with black text
                prescriptionTable.addCell(new Cell()
                        .add(new Paragraph(values[i])
                                .setFontSize(12)
                                .setFontColor(ColorConstants.BLACK))
                        .setBackgroundColor(i % 2 == 0 ? ColorConstants.WHITE : EVEN_ROW_COLOR)
                        .setPadding(10)
                        .setBorder(new SolidBorder(BORDER_COLOR, 1)));
            }
            document.add(prescriptionTable);

            // Footer (moved lower on the page)
            Table footerTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            footerTable.setBackgroundColor(HEADER_FOOTER_COLOR);
            footerTable.setBorder(new SolidBorder(BORDER_COLOR, 1));
            footerTable.setMarginTop(100);
            Cell footerCell = new Cell()
                    .add(new Paragraph("Merci de faire confiance à notre service médical.")
                            .setFontSize(14)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER))
                    .add(new Paragraph("© " + LocalDate.now().getYear() + " Votre Site Médical. Tous droits réservés.")
                            .setFontSize(14)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setPadding(15)
                    .setBorder(Border.NO_BORDER);
            footerTable.addCell(footerCell);
            document.add(footerTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void sortAscending() {
        dateColumn.setSortType(TableColumn.SortType.ASCENDING);
        prescriptionsTable.getSortOrder().clear();
        prescriptionsTable.getSortOrder().add(dateColumn);
        prescriptionsTable.sort();
    }

    @FXML
    private void sortDescending() {
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        prescriptionsTable.getSortOrder().clear();
        prescriptionsTable.getSortOrder().add(dateColumn);
        prescriptionsTable.sort();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            try {
                dialogPane.getStylesheets().add(getClass().getResource("/css/Prescription.css").toExternalForm());
                dialogPane.getStyleClass().add("custom-alert");
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du CSS : " + e.getMessage());
            }

            alert.showAndWait();
        });
    }
}