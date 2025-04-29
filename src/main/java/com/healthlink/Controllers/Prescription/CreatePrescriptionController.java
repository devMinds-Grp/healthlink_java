package com.healthlink.Controllers.Prescription;

import com.healthlink.Entities.Prescription;
import com.healthlink.Services.PrescriptionService;
import com.healthlink.Services.AppointmentService;
import com.healthlink.Entities.Appointment;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreatePrescriptionController {

    @FXML private TextField nomMedicamentField;
    @FXML private TextField dosageField;
    @FXML private TextField dureeField;
    @FXML private TextArea notesArea;
    @FXML private ComboBox<Integer> rdvCardIdCombo;

    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final AppointmentService appointmentService = new AppointmentService();
    private List<Appointment> appointments;

    @FXML
    public void initialize() {
        setupRdvCardIdComboBox();
    }

    private void setupRdvCardIdComboBox() {
        List<Integer> appointmentIds = new ArrayList<>();
        appointmentIds.add(null); // Allow null selection
        appointments = appointmentService.getAllAppointments();
        for (Appointment appointment : appointments) {
            appointmentIds.add(appointment.getId());
        }
        rdvCardIdCombo.setItems(FXCollections.observableArrayList(appointmentIds));

        // Custom cell factory to display the date alongside the ID
        rdvCardIdCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Appointment appointment = appointments.stream()
                            .filter(a -> a.getId() == item)
                            .findFirst()
                            .orElse(null);
                    if (appointment != null) {
                        setText(appointment.getDate());
                    } else {
                        setText("ID: " + item);
                    }
                }
            }
        });

        // Ensure the button cell (the displayed value when not expanded) also shows the date
        rdvCardIdCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Appointment appointment = appointments.stream()
                            .filter(a -> a.getId() == item)
                            .findFirst()
                            .orElse(null);
                    if (appointment != null) {
                        setText("Date: " + appointment.getDate());
                    } else {
                        setText("ID: " + item);
                    }
                }
            }
        });

        rdvCardIdCombo.getSelectionModel().selectFirst(); // Select null by default
    }

    @FXML
    public void handleSaveAction(ActionEvent actionEvent) {
        if (!validateForm()) {
            return;
        }

        try {
            Prescription prescription = createPrescriptionFromForm();
            boolean success = prescriptionService.addPrescription(prescription);

            if (success) {
                showAlert("Succès", "Prescription créée avec succès", Alert.AlertType.INFORMATION);
                navigateToListView();
            } else {
                showAlert("Erreur", "Échec de la création de la prescription.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur Critique", "Une erreur inattendue s'est produite : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        // Validate nomMedicament
        String nomMedicament = nomMedicamentField.getText();
        if (nomMedicament == null || nomMedicament.trim().isEmpty()) {
            showAlert("Champ requis", "Veuillez entrer le nom du médicament", Alert.AlertType.WARNING);
            return false;
        }
        if (nomMedicament.trim().length() < 5) {
            showAlert("Erreur", "Le nom du médicament doit contenir au moins 5 caractères.", Alert.AlertType.WARNING);
            return false;
        }

        // Validate dosage
        String dosage = dosageField.getText();
        if (dosage == null || dosage.trim().isEmpty()) {
            showAlert("Champ requis", "Veuillez entrer le dosage", Alert.AlertType.WARNING);
            return false;
        }
        if (dosage.trim().length() < 5) {
            showAlert("Erreur", "Le dosage doit contenir au moins 5 caractères.", Alert.AlertType.WARNING);
            return false;
        }

        // Validate duree
        String duree = dureeField.getText();
        if (duree == null || duree.trim().isEmpty()) {
            showAlert("Champ requis", "Veuillez entrer la durée", Alert.AlertType.WARNING);
            return false;
        }
        if (duree.trim().length() < 5) {
            showAlert("Erreur", "La durée doit contenir au moins 5 caractères.", Alert.AlertType.WARNING);
            return false;
        }

        // Validate notes
        String notes = notesArea.getText();
        if (notes == null || notes.trim().isEmpty()) {
            showAlert("Champ requis", "Veuillez entrer des notes", Alert.AlertType.WARNING);
            return false;
        }
        if (notes.trim().length() < 10) {
            showAlert("Erreur", "Les notes doivent contenir au moins 10 caractères.", Alert.AlertType.WARNING);
            return false;
        }

        // Validate rdvCardId
        Integer rdvCardId = rdvCardIdCombo.getValue();
        if (rdvCardId == null) {
            showAlert("Champ requis", "Veuillez sélectionner un rendez-vous.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private Prescription createPrescriptionFromForm() {
        String nomMedicament = nomMedicamentField.getText().trim();
        String dosage = dosageField.getText().trim();
        String duree = dureeField.getText().trim();
        String notes = notesArea.getText().trim();
        Integer rdvCardId = rdvCardIdCombo.getValue();

        return new Prescription(nomMedicament, dosage, duree, notes, rdvCardId);
    }

    private void navigateToListView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Prescription/list_prescription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomMedicamentField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Prescriptions");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la liste: " + e.getMessage(), Alert.AlertType.ERROR);
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