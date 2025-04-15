package com.healthlink.Controllers.Appoitment;

import com.healthlink.Entities.Appointment;
import com.healthlink.Services.AppointmentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Update_appointmentController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> typeCombo;

    private Appointment currentAppointment;
    private final AppointmentService appointmentService = new AppointmentService();
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final String DB_DATE_PATTERN = "yyyy-MM-dd";
    private static final String ONLINE_APPOINTMENT = "En ligne";
    private static final String IN_PERSON_APPOINTMENT = "Présentielle";

    public void setAppointment(Appointment appointment) {
        this.currentAppointment = appointment;
        if (appointment != null) {
            LocalDate date = LocalDate.parse(appointment.getDate(), DateTimeFormatter.ofPattern(DB_DATE_PATTERN));
            datePicker.setValue(date);
            String type = appointment.getType().equals("en_ligne") ? ONLINE_APPOINTMENT : IN_PERSON_APPOINTMENT;
            typeCombo.setValue(type);
        }
    }

    @FXML
    public void initialize() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        datePicker.setConverter(new StringConverter<>() {
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

        typeCombo.getItems().addAll(ONLINE_APPOINTMENT, IN_PERSON_APPOINTMENT);
    }

    @FXML
    public void handleUpdate() {
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

        if (!date.equals(currentAppointment.getDate()) || !type.equals(currentAppointment.getType())) {
            currentAppointment.setDate(date);
            currentAppointment.setType(type);
            if (appointmentService.updateAppointment(currentAppointment)) {
                String formattedDate = dateValue.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
                String message = String.format("Le rendez-vous %s du %s a été mis à jour avec succès !",
                        typeValue, formattedDate);
                showAlert("Succès", message, Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Erreur", "Échec de la mise à jour du rendez-vous.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Information", "Aucune modification n'a été effectuée.", Alert.AlertType.INFORMATION);
            closeWindow();
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        datePicker.getScene().getWindow().hide();
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