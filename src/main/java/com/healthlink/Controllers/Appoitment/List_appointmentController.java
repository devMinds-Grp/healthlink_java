package com.healthlink.Controllers.Appoitment;

import com.healthlink.Entities.Appointment;
import com.healthlink.Services.AppointmentService;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class List_appointmentController {

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TextField searchField;
    @FXML private Button sortAscButton;
    @FXML private Button sortDescButton;

    private final AppointmentService appointmentService = new AppointmentService();
    private ObservableList<Appointment> appointmentList;
    private FilteredList<Appointment> filteredAppointments;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAppointments();
        setupSearchFilter();
        setupButtonActions();
    }

    private void setupSearchFilter() {
        filteredAppointments = new FilteredList<>(appointmentList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredAppointments.setPredicate(appointment -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();
                    return (appointment.getPatientName() != null && appointment.getPatientName().toLowerCase().contains(lowerCaseFilter)) ||
                            (appointment.getDoctorName() != null && appointment.getDoctorName().toLowerCase().contains(lowerCaseFilter)) ||
                            (appointment.getDate() != null && appointment.getDate().toLowerCase().contains(lowerCaseFilter));
                })
        );

        SortedList<Appointment> sortedAppointments = new SortedList<>(filteredAppointments);
        sortedAppointments.comparatorProperty().bind(appointmentsTable.comparatorProperty());
        appointmentsTable.setItems(sortedAppointments);
    }

    private void setupButtonActions() {
        sortAscButton.setOnAction(event -> sortAscending());
        sortDescButton.setOnAction(event -> sortDescending());
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
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage(), Alert.AlertType.ERROR);
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
        TableColumn<Appointment, String> patientNameColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(0);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> doctorNameColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(1);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> dateColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(2);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> typeColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(3);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> statusColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(4);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, Void> actionsColumn = (TableColumn<Appointment, Void>) appointmentsTable.getColumns().get(5);

        patientNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        patientNameColumn.setCellFactory(column -> new TableCell<Appointment, String>() {
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

        doctorNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctorName()));
        doctorNameColumn.setCellFactory(column -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Médecin inconnu")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getType().equals("en_ligne") ? "En ligne" : "Présentielle"
        ));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getValue()));

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
        Utilisateur connectedUser = AuthService.getConnectedUtilisateur();
        if (connectedUser == null || connectedUser.getRole().getId() != 3) {
            showAlert("Erreur", "Aucun patient connecté ou rôle non autorisé.", Alert.AlertType.ERROR);
            appointmentList = FXCollections.observableArrayList();
        } else {
            List<Appointment> appointments = appointmentService.getAppointmentsByConnectedUser();
            if (appointmentList == null) {
                appointmentList = FXCollections.observableArrayList(appointments);
                setupSearchFilter();
            } else {
                appointmentList.setAll(appointments);
            }
        }
    }

    @FXML
    private void sortAscending() {
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> dateColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(2);
        appointmentsTable.getSortOrder().clear();
        dateColumn.setSortType(TableColumn.SortType.ASCENDING);
        appointmentsTable.getSortOrder().add(dateColumn);
        appointmentsTable.sort();
    }

    @FXML
    private void sortDescending() {
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> dateColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(2);
        appointmentsTable.getSortOrder().clear();
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        appointmentsTable.getSortOrder().add(dateColumn);
        appointmentsTable.sort();
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