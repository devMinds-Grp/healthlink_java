package com.healthlink.Controllers.Appoitment;

import com.healthlink.Entities.Appointment;
import com.healthlink.Services.AppointmentService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class List_appointmentController {

    @FXML private TableView<Appointment> appointmentsTable;

    private final AppointmentService appointmentService = new AppointmentService();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAppointments();
    }

    private void openEditDialog(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Appointment/update_appoitment.fxml"));
            Parent root = loader.load();

            Update_appointmentController controller = loader.getController();
            controller.setAppointment(appointment);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAppointments();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteAppointment(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le rendez-vous");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce rendez-vous du " + appointment.getDate() + "?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/Appointment.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (appointmentService.deleteAppointment(appointment.getId())) {
                    showAlert("Succès", "Le rendez-vous du " + appointment.getDate() + " a été supprimé avec succès !", Alert.AlertType.INFORMATION);
                    loadAppointments();
                } else {
                    showAlert("Erreur", "Échec de la suppression du rendez-vous.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void setupTableColumns() {
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> dateColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(0);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> typeColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(1);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> statusColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(2);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, Void> actionsColumn = (TableColumn<Appointment, Void>) appointmentsTable.getColumns().get(3);

        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getType().equals("en_ligne") ? "En ligne" : "Présentielle"
        ));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        actionsColumn.setCellFactory(col -> new TableCell<Appointment, Void>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("edit-btn");
                deleteBtn.getStyleClass().add("delete-btn");

                editBtn.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    openEditDialog(appointment);
                });

                deleteBtn.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    deleteAppointment(appointment);
                });

                buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void loadAppointments() {
        appointmentsTable.getItems().clear();
        List<Appointment> appointments = appointmentService.getAllAppointments();
        appointmentsTable.getItems().addAll(appointments);
    }

    @FXML
    private void showCreateForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Appointment/create_appointment.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAppointments();
        } catch (IOException e) {
            e.printStackTrace();
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