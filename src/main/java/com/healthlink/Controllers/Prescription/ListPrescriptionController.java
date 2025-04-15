package com.healthlink.Controllers.Prescription;

import com.healthlink.Entities.Prescription;
import com.healthlink.Services.PrescriptionService;
import com.healthlink.Services.AppointmentService;
import com.healthlink.Entities.Appointment;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ListPrescriptionController {

    @FXML private TableView<Prescription> appointmentsTable;
    @FXML private TableColumn<Prescription, String> nomMedicamentColumn;
    @FXML private TableColumn<Prescription, String> dosageColumn;
    @FXML private TableColumn<Prescription, String> dureeColumn;
    @FXML private TableColumn<Prescription, String> notesColumn;
    @FXML private TableColumn<Prescription, String> rdvCardIdColumn; // Changed from appointmentDateColumn
    @FXML private TableColumn<Prescription, Void> actionsColumn;

    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final ObservableList<Prescription> prescriptionsData = FXCollections.observableArrayList();
    private List<Appointment> appointments; // Store appointments for mapping

    @FXML
    public void initialize() {
        appointments = appointmentService.getAllAppointments(); // Load appointments for mapping
        setupPrescriptionsTable();
        loadPrescriptionsData();
    }

    private void setupPrescriptionsTable() {
        nomMedicamentColumn.setCellValueFactory(new PropertyValueFactory<>("nomMedicament"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        rdvCardIdColumn.setCellValueFactory(cellData -> {
            Integer rdvCardId = cellData.getValue().getRdvCardId();
            if (rdvCardId == null) {
                return new SimpleStringProperty("None");
            }
            Appointment appointment = appointments.stream()
                    .filter(a -> a.getId() == rdvCardId)
                    .findFirst()
                    .orElse(null);
            if (appointment != null) {
                return new SimpleStringProperty( appointment.getDate() );
            }
            return new SimpleStringProperty("ID: " + rdvCardId);
        });
        setupActionsColumn();
        appointmentsTable.setItems(prescriptionsData);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox pane = new HBox( editButton, deleteButton);

            {
                pane.setSpacing(5);

                editButton.getStyleClass().add("edit-btn");
                deleteButton.getStyleClass().add("delete-btn");

                editButton.setOnAction(event -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    showEditForm(prescription);
                });

                deleteButton.setOnAction(event -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    confirmAndDeletePrescription(prescription);
                });


            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadPrescriptionsData() {
        prescriptionsData.clear();
        prescriptionsData.addAll(prescriptionService.getAllPrescriptions());
        if (prescriptionsData.isEmpty()) {
            showAlert("Avertissement", "Aucune prescription chargée.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void showCreateForm(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Prescription/create_prescription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) appointmentsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer une Prescription");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showEditForm(Prescription prescription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Prescription/update_prescription.fxml"));
            Parent root = loader.load();
            UpdatePrescriptionController controller = loader.getController();
            controller.setPrescription(prescription);
            Stage stage = (Stage) appointmentsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier la Prescription");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void confirmAndDeletePrescription(Prescription prescription) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la prescription");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette prescription ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/Appointment.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-alert");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = prescriptionService.deletePrescription(prescription.getId());
            if (success) {
                showAlert("Succès", "Prescription supprimée avec succès", Alert.AlertType.INFORMATION);
                loadPrescriptionsData();
            } else {
                showAlert("Erreur", "Échec de la suppression de la prescription.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    getClass().getResource("/css/Appointment.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("custom-alert");

            alert.showAndWait();
        });
    }
}