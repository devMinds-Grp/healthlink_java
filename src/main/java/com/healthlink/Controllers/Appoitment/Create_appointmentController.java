package com.healthlink.Controllers.Appoitment;

import com.healthlink.Entities.Appointment;
import com.healthlink.Services.AppointmentService;
import com.healthlink.Services.AppointmentService.AddResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Create_appointmentController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Label minDateLabel;

    private final AppointmentService appointmentService = new AppointmentService();
    private static final String DB_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_PATTERN = "dd/MM/yyyy";

    @FXML
    public void initialize() {
        setupDatePicker();
        setupTypeComboBox();
        minDateLabel.setStyle("-fx-text-fill: red;");
    }

    private void setupDatePicker() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ?
                        LocalDate.parse(string, dateFormatter) : null;
            }
        });

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });

        datePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBefore(LocalDate.now())) {
                minDateLabel.setStyle("-fx-text-fill: green;");
            } else {
                minDateLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }

    private void setupTypeComboBox() {
        typeCombo.getItems().clear();
        typeCombo.getItems().addAll("En ligne", "Présentielle");
        typeCombo.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleSave() {
        LocalDate dateValue = datePicker.getValue();
        String typeValue = typeCombo.getValue();

        if (dateValue == null) {
            showAlert("Erreur", "Veuillez sélectionner une date.", Alert.AlertType.WARNING);
            return;
        }
        if (dateValue.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date doit être aujourd'hui ou une date future.", Alert.AlertType.WARNING);
            return;
        }
        if (typeValue == null || typeValue.trim().isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner un type de rendez-vous.", Alert.AlertType.WARNING);
            return;
        }

        String date = dateValue.format(DateTimeFormatter.ofPattern(DB_DATE_PATTERN));
        String type = typeValue.toLowerCase().replace(" ", "_").replace("é", "e");
        Appointment newAppointment = new Appointment(date, type);

        System.out.println("Tentative d'ajout d'un rendez-vous :");
        System.out.println("Date : " + newAppointment.getDate());
        System.out.println("Type : " + newAppointment.getType());
        System.out.println("Statut : " + newAppointment.getStatus());

        AddResult result = appointmentService.addAppointment(newAppointment);
        if (result.isSuccess()) {
            String formattedDate = dateValue.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
            String message = String.format("Votre rendez-vous %s du %s a été créé avec succès !",
                    typeValue, formattedDate);
            showAlert("Succès", message, Alert.AlertType.INFORMATION);
            closeWindow();
        } else {
            showAlert("Erreur", "Échec de la création du rendez-vous : " + result.getErrorMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
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