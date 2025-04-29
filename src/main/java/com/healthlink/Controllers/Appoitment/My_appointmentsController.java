package com.healthlink.Controllers.Appoitment;

import com.healthlink.Entities.Appointment;
import com.healthlink.Entities.AppointmentStatus;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AppointmentService;
import com.healthlink.Services.AuthService;
import com.healthlink.Services.SMSService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class My_appointmentsController {

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TextField searchField;
    @FXML private Button sortAscButton;
    @FXML private Button sortDescButton;

    private final AppointmentService appointmentService = new AppointmentService();
    private final SMSService smsService = new SMSService();
    private ObservableList<Appointment> appointmentList;
    private FilteredList<Appointment> filteredAppointments;
    // Track appointments for which a reminder has been sent
    private final Set<Integer> remindersSent = new HashSet<>();

    @FXML
    public void initialize() {
        Utilisateur doctor = AuthService.getConnectedUtilisateur();
        if (doctor == null || doctor.getRole().getId() != 2) {
            showAlert("Erreur", "Utilisateur non autorisé ou non connecté.", Alert.AlertType.ERROR);
            return;
        }

        sortAscButton.setOnAction(e -> sortAscending());
        sortDescButton.setOnAction(e -> sortDescending());

        setupTableColumns();
        loadDoctorAppointments(doctor.getId());
        setupSearchFilter();
    }

    private void loadDoctorAppointments(int doctorId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        if (appointments == null || appointments.isEmpty()) {
            showAlert("Information", "Aucun rendez-vous trouvé.", Alert.AlertType.INFORMATION);
            appointmentList = FXCollections.observableArrayList();
        } else {
            appointmentList = FXCollections.observableArrayList(appointments);
        }
        setupSearchFilter();
        appointmentsTable.refresh();
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
                            (appointment.getDate() != null && appointment.getDate().toLowerCase().contains(lowerCaseFilter));
                })
        );

        SortedList<Appointment> sortedAppointments = new SortedList<>(filteredAppointments);
        sortedAppointments.comparatorProperty().bind(appointmentsTable.comparatorProperty());
        appointmentsTable.setItems(sortedAppointments);
        appointmentsTable.refresh();
    }

    private void setupTableColumns() {
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> patientNameColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(0);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> dateColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(1);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> typeColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(2);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> statusColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(3);
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, Void> actionsColumn = (TableColumn<Appointment, Void>) appointmentsTable.getColumns().get(4);

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

        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getType().equals("en_ligne") ? "En ligne" : "Présentielle"
        ));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getValue()));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approuver");
            private final Button refuseBtn = new Button("Refuser");
            private final Button reminderBtn = new Button("Rappeler");
            private final HBox buttonsBox = new HBox(10);

            {
                approveBtn.getStyleClass().add("approve-btn");
                refuseBtn.getStyleClass().add("refuse-btn");
                reminderBtn.getStyleClass().add("reminder-btn");

                approveBtn.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    approveAppointment(appointment);
                });

                refuseBtn.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    refuseAppointment(appointment);
                });

                reminderBtn.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    sendReminder(appointment);
                    // Disable the button after clicking
                    reminderBtn.setDisable(true);
                });

                buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();
                    if (appointment.getStatus() == AppointmentStatus.EN_ATTENTE) {
                        buttonsBox.getChildren().addAll(approveBtn, refuseBtn);
                        setGraphic(buttonsBox);
                    } else if (appointment.getStatus() == AppointmentStatus.CONFIRME) {
                        // Check if a reminder has already been sent for this appointment
                        reminderBtn.setDisable(remindersSent.contains(appointment.getId()));
                        buttonsBox.getChildren().add(reminderBtn);
                        setGraphic(buttonsBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        appointmentsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Appointment appointment, boolean empty) {
                super.updateItem(appointment, empty);
                if (appointment == null || empty) {
                    getStyleClass().remove("highlight-row");
                    setStyle("");
                } else {
                    boolean isConfirmed = appointment.getStatus() == AppointmentStatus.CONFIRME;
                    boolean isNextDay = false;
                    try {
                        LocalDate today = LocalDate.now();
                        LocalDate nextDay = today.plusDays(1);
                        String dateString = appointment.getDate();

                        if (dateString == null || dateString.trim().isEmpty()) {
                            System.err.println("Invalid date for appointment ID " + appointment.getId() + ": Date is null or empty");
                        } else {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            try {
                                LocalDate appointmentDate = LocalDate.parse(dateString, formatter);
                                isNextDay = appointmentDate.equals(nextDay);
                            } catch (Exception e) {
                                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                try {
                                    LocalDate appointmentDate = LocalDate.parse(dateString, formatter);
                                    isNextDay = appointmentDate.equals(nextDay);
                                } catch (Exception ex) {
                                    System.err.println("Error parsing date for appointment ID " + appointment.getId() + ": " + dateString + " - " + ex.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Unexpected error for appointment ID " + appointment.getId() + ": " + e.getMessage());
                    }

                    if (isConfirmed && isNextDay) {
                        if (!getStyleClass().contains("highlight-row")) {
                            getStyleClass().add("highlight-row");
                        }
                    } else {
                        getStyleClass().remove("highlight-row");
                    }
                }
            }
        });
    }

    private void approveAppointment(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Approuver le rendez-vous");
        alert.setContentText("Êtes-vous sûr de vouloir approuver ce rendez-vous du " + appointment.getDate() + "?");

        DialogPane dialogPane = alert.getDialogPane();
        try {
            URL cssResource = getClass().getResource("/css/Appointment.css");
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("custom-alert");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS pour la popup de confirmation : " + e.getMessage());
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                appointment.setStatus(AppointmentStatus.CONFIRME.getValue());

                if (appointmentService.updateAppointment(appointment)) {
                    String formattedPhone = formatPhoneNumber(appointment.getPatientPhone());
                    if (formattedPhone != null && !formattedPhone.isEmpty()) {
                        try {
                            smsService.sendConfirmationMessage(
                                    formattedPhone,
                                    appointment.getDoctorName(),
                                    appointment.getDate()
                            );
                            System.out.println("Confirmation SMS sent successfully to: " + formattedPhone);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'envoi du SMS de confirmation: " + e.getMessage());
                            e.printStackTrace();
                            showAlert("Avertissement", "Le rendez-vous a été confirmé, mais l'envoi du SMS a échoué: " + e.getMessage(), Alert.AlertType.WARNING);
                        }
                    } else {
                        System.err.println("Invalid or missing phone number for appointment ID: " + appointment.getId());
                        showAlert("Avertissement", "Le rendez-vous a été confirmé, mais le numéro de téléphone est invalide ou manquant.", Alert.AlertType.WARNING);
                    }

                    Utilisateur doctor = AuthService.getConnectedUtilisateur();
                    if (doctor != null) {
                        loadDoctorAppointments(doctor.getId());
                    }
                    showAlert("Succès", "Le rendez-vous a été confirmé avec succès. Un rappel SMS sera envoyé la veille.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Échec de la confirmation du rendez-vous.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void refuseAppointment(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Refuser le rendez-vous");
        alert.setContentText("Êtes-vous sûr de vouloir refuser ce rendez-vous du " + appointment.getDate() + "?");

        DialogPane dialogPane = alert.getDialogPane();
        try {
            URL cssResource = getClass().getResource("/css/Appointment.css");
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("custom-alert");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS pour la popup de confirmation : " + e.getMessage());
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                appointment.setStatus(AppointmentStatus.ANNULE.getValue());

                if (appointmentService.updateAppointment(appointment)) {
                    String formattedPhone = formatPhoneNumber(appointment.getPatientPhone());
                    if (formattedPhone != null && !formattedPhone.isEmpty()) {
                        try {
                            smsService.sendCancellationMessage(
                                    formattedPhone,
                                    appointment.getDoctorName(),
                                    appointment.getDate()
                            );
                            System.out.println("Cancellation SMS sent successfully to: " + formattedPhone);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'envoi du SMS d'annulation: " + e.getMessage());
                            e.printStackTrace();
                            showAlert("Avertissement", "Le rendez-vous a été annulé, mais l'envoi du SMS a échoué.", Alert.AlertType.WARNING);
                        }
                    }

                    Utilisateur doctor = AuthService.getConnectedUtilisateur();
                    if (doctor != null) {
                        loadDoctorAppointments(doctor.getId());
                    }
                    showAlert("Succès", "Le rendez-vous a été annulé avec succès.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Échec de l'annulation du rendez-vous.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void sendReminder(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.CONFIRME) {
            showAlert("Erreur", "Le rappel ne peut être envoyé que pour les rendez-vous confirmés.", Alert.AlertType.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Envoyer un rappel");
        alert.setContentText("Êtes-vous sûr de vouloir envoyer un SMS de rappel pour ce rendez-vous du " + appointment.getDate() + "?");

        DialogPane dialogPane = alert.getDialogPane();
        try {
            URL cssResource = getClass().getResource("/css/Appointment.css");
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
                dialogPane.getStyleClass().add("custom-alert");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS pour la popup de confirmation : " + e.getMessage());
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String formattedPhone = formatPhoneNumber(appointment.getPatientPhone());
                if (formattedPhone != null && !formattedPhone.isEmpty()) {
                    try {
                        smsService.sendReminderMessage(
                                formattedPhone,
                                appointment.getDoctorName(),
                                appointment.getDate()
                        );
                        System.out.println("Reminder SMS sent successfully to: " + formattedPhone);
                        // Mark this appointment as having sent a reminder
                        remindersSent.add(appointment.getId());
                        showAlert("Succès", "Le SMS de rappel a été envoyé avec succès.", Alert.AlertType.INFORMATION);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'envoi du SMS de rappel: " + e.getMessage());
                        e.printStackTrace();
                        showAlert("Erreur", "Échec de l'envoi du SMS de rappel : " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Erreur", "Numéro de téléphone du patient non disponible ou invalide.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void sortAscending() {
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> dateColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(1);
        appointmentsTable.getSortOrder().clear();
        dateColumn.setSortType(TableColumn.SortType.ASCENDING);
        appointmentsTable.getSortOrder().add(dateColumn);
        appointmentsTable.sort();
    }

    @FXML
    private void sortDescending() {
        @SuppressWarnings("unchecked")
        TableColumn<Appointment, String> dateColumn = (TableColumn<Appointment, String>) appointmentsTable.getColumns().get(1);
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
            try {
                URL cssResource = getClass().getResource("/css/Appointment.css");
                if (cssResource != null) {
                    dialogPane.getStylesheets().add(cssResource.toExternalForm());
                    dialogPane.getStyleClass().add("custom-alert");
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du CSS : " + e.getMessage());
            }

            alert.showAndWait();
        });
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            String digits = phoneNumber.replaceAll("[^0-9]", "");
            if (digits.matches("\\d{8}")) {
                return "+216" + digits;
            }
            if (phoneNumber.matches("\\+\\d{10,15}")) {
                return phoneNumber;
            }
        }
        System.err.println("Invalid phone number format: " + phoneNumber);
        return null;
    }
}