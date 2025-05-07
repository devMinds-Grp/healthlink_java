package com.healthlink.Controllers;

import com.healthlink.Entites.DonDuSang;
import com.healthlink.Services.DonDuSangService;
import com.healthlink.utils.MyDB;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ListDonController {

    @FXML
    private TableView<DonDuSang> donationTable;

    @FXML
    private TableColumn<DonDuSang, String> descriptionColumn;

    @FXML
    private TableColumn<DonDuSang, String> lieuColumn;

    @FXML
    private TableColumn<DonDuSang, LocalDate> dateColumn;

    @FXML
    private TableColumn<DonDuSang, String> numTelColumn;

    @FXML
    private TableColumn<DonDuSang, Void> actionsColumn;

    private DonDuSangService donDuSangService;
    private ObservableList<DonDuSang> donationList;

    @FXML
    private TextArea chatbotResponseArea;

    @FXML
    private Button chatbotSubmitButton;

    @FXML
    private ComboBox<String> chatbotInputField;



    @FXML
    public void initialize() {
        try {
            Connection connection = MyDB.getInstance().getConnection();
            if (connection == null) {
                throw new SQLException("Failed to establish database connection");
            }
            donDuSangService = new DonDuSangService(connection);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database: " + e.getMessage());
            return;
        }

        // Initialize table columns
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        lieuColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLieu()));
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        numTelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumTel()));

        // Set up actions column with Edit, Delete, Add Response, and Share Facebook buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button addResponseButton = new Button("Add Response");
            private final Button shareFacebookButton = new Button("Share FB");
            private final HBox hbox = new HBox(5, editButton, deleteButton, addResponseButton, shareFacebookButton);

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                addResponseButton.getStyleClass().add("add-response-button");
                shareFacebookButton.getStyleClass().add("share-facebook-button");
                editButton.setOnAction(event -> {
                    DonDuSang don = getTableView().getItems().get(getIndex());
                    if (don == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a donation to edit.");
                        return;
                    }
                    openEditDonWindow(don);
                });
                deleteButton.setOnAction(event -> {
                    DonDuSang don = getTableView().getItems().get(getIndex());
                    if (don == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a donation to delete.");
                        return;
                    }
                    deleteDonation(don);
                });
                addResponseButton.setOnAction(event -> {
                    DonDuSang don = getTableView().getItems().get(getIndex());
                    if (don == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a donation to add a response.");
                        return;
                    }
                    openAddReponseDonWindow(don);
                });
                shareFacebookButton.setOnAction(event -> {
                    DonDuSang don = getTableView().getItems().get(getIndex());
                    if (don == null) {
                        showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a donation to share.");
                        return;
                    }
                    shareDonation(don);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // Load data into the table
        refreshTable();

        // Populate the ComboBox with suggested questions (matching generateChatbotResponse)
        ObservableList<String> suggestedQuestions = FXCollections.observableArrayList(
                "Can I donate after traveling abroad?",
                "How long before I can donate again?",
                "What should I do before donating?",
                "Can I donate if I have a cold?",
                "How old do I need to be to donate?",
                "What’s the minimum weight to donate?",
                "Can I donate if I’m on medication?",
                "Can I donate after getting a tattoo?",
                "What should I do after donating?",
                "Can I donate if I’m pregnant?",
                "Can I drink alcohol before donating?",
                "What if I have low iron?",
                "Can I donate if I have diabetes?"
        );
        chatbotInputField.setItems(suggestedQuestions);
    }

    @FXML
    private void openAddDonWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_don.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add New Blood Donation");
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Error opening add window: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add window: " + e.getMessage());
        }
    }

    private void openEditDonWindow(DonDuSang don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/edit_don.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Blood Donation");

            EditDonController controller = loader.getController();
            controller.setDonDuSang(don);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Error opening edit window: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit window: " + e.getMessage());
        }
    }

    private void openAddReponseDonWindow(DonDuSang don) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_reponse_don.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add Response to Donation");

            AddReponseDonController controller = loader.getController();
            controller.setBloodDonationId(don.getId());
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error opening add response window: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add response window: " + e.getMessage());
        }
    }

    private void deleteDonation(DonDuSang don) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this donation?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (donDuSangService.deleteDonDuSang(don.getId())) {
                    donationList.remove(don);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Donation deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete donation from the database.");
                }
            }
        });
    }

    private void shareDonation(DonDuSang don) {
        new Thread(() -> {
            try {
                String pageAccessToken = "EAAJhCq7tTFkBO6rkEKuQ25ud57xKMikioE6ZBbpE9ZAA85TsZB2T8gI7YcJS0ZBs4DFzUKneglakrlXnFCGhN9ifrFPBkg3ITSvVQ4OP9pwE4MupTflwQUsJChTHcOrIVVC8e63jrFV5kpjWTr3xULr9OLSbKgpvQiYEMx4XpZAXHc5MZAuWYFCUp8AEoZAaaPx4KaPGNC2p0rDuHkusNNqoFwD";
                String pageId = "665252196665912";
                String description = don.getDescription().length() > 100 ? don.getDescription().substring(0, 97) + "..." : don.getDescription();
                String message = String.format("Urgent: Blood donation needed! %s\nLocation: %s\nDate: %s\nContact: %s\n#BloodDonation",
                        description, don.getLieu(), don.getDate().toString(), don.getNumTel());

                HttpClient client = HttpClient.newHttpClient();
                String postUrl = String.format("https://graph.facebook.com/%s/feed", pageId);
                String postBody = String.format("message=%s&access_token=%s",
                        URLEncoder.encode(message, StandardCharsets.UTF_8), pageAccessToken);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(postUrl))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(postBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Donation shared successfully on Facebook!");
                    });
                } else {
                    JSONObject errorJson = new JSONObject(response.body());
                    throw new Exception("Facebook API error: " + errorJson.getJSONObject("error").getString("message"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to share donation: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void refreshTable() {
        try {
            donationList = FXCollections.observableArrayList(donDuSangService.getAllDonDuSang());
            donationTable.setItems(donationList);
        } catch (Exception e) {
            System.err.println("Error refreshing table: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh donation list: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleChatbotQuestion() {
        String question = chatbotInputField.getEditor().getText().trim().toLowerCase();
        if (question.isEmpty()) {
            chatbotResponseArea.setText("Please select or type a question.");
            return;
        }

        String response = generateChatbotResponse(question);
        chatbotResponseArea.setText(response);
        chatbotInputField.getEditor().clear();
    }

    private String generateChatbotResponse(String question) {
        // Rule-based responses for common donor questions
        if (question.contains("voyage") || question.contains("travel")) {
            return "If you’ve traveled abroad recently, you may need to wait before donating. For example, travel to areas with malaria risk requires a 12-month deferral. Please check with your local blood donation center for specific restrictions.";
        } else if (question.contains("combien de temps") || question.contains("how long") || question.contains("redonner") || question.contains("donate again")) {
            // Check donation history for the user (assuming all donations in the table are from the current user)
            if (donationList.isEmpty()) {
                return "You haven't donated yet. You can donate whole blood every 56 days (8 weeks) for men and 84 days (12 weeks) for women, or as advised by your doctor.";
            } else {
                // Find the most recent donation
                LocalDate lastDonationDate = donationList.stream()
                        .map(DonDuSang::getDate)
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now());
                long daysSinceLastDonation = ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now());
                long daysToWait = 56 - daysSinceLastDonation; // Assuming 56 days for men (simplified)
                if (daysToWait <= 0) {
                    return "You can donate again now! It’s been " + daysSinceLastDonation + " days since your last donation.";
                } else {
                    return "You need to wait " + daysToWait + " more days before you can donate again. It’s been " + daysSinceLastDonation + " days since your last donation.";
                }
            }
        } else if (question.contains("hydratation") || question.contains("hydration") || question.contains("avant") || question.contains("before")) {
            return "It’s important to stay well-hydrated before donating. Drink plenty of water (at least 16 ounces) a few hours before your donation, and avoid caffeine to ensure a smooth donation process.";
        } else if (question.contains("cold") || question.contains("sick") || question.contains("illness")) {
            return "You should wait until you’re fully recovered from a cold or illness before donating. Generally, you need to be symptom-free for at least 7 days. Check with your donation center for specific guidelines.";
        } else if (question.contains("age") || question.contains("old")) {
            return "To donate blood, you typically need to be between 17 and 65 years old. Some centers allow younger donors (16 with parental consent) or older donors if they’re in good health. Contact your local center to confirm.";
        } else if (question.contains("weight") || question.contains("heavy")) {
            return "You need to weigh at least 50 kg (110 lbs) to donate blood. This ensures your body can handle the donation safely. If you’re unsure, check with your donation center.";
        } else if (question.contains("medication") || question.contains("medicine") || question.contains("pills")) {
            return "Some medications may affect your eligibility to donate. For example, blood thinners or certain antibiotics may require a deferral period. Please provide a list of your medications to the donation center staff for a proper assessment.";
        } else if (question.contains("tattoo") || question.contains("piercing")) {
            return "If you’ve had a tattoo or piercing, you may need to wait 6 to 12 months before donating, depending on local regulations and the sterility of the procedure. Check with your donation center for their specific policy.";
        } else if (question.contains("after") || question.contains("post") || question.contains("recovery")) {
            return "After donating, rest for at least 10-15 minutes at the center, drink plenty of fluids, and avoid strenuous activity for the rest of the day. You might feel tired, so take it easy and eat a nutritious meal.";
        } else if (question.contains("pregnant") || question.contains("pregnancy")) {
            return "You cannot donate blood during pregnancy. After giving birth, you typically need to wait 6 months before donating to ensure your body has fully recovered. Consult your doctor if you’re unsure.";
        } else if (question.contains("alcohol") || question.contains("drink")) {
            return "Avoid drinking alcohol for at least 24 hours before donating, as it can dehydrate you and affect the donation process. After donating, wait 48 hours before consuming alcohol to support your recovery.";
        } else if (question.contains("iron") || question.contains("anemia")) {
            return "You need to have sufficient iron levels to donate. If you have anemia or low hemoglobin, you won’t be able to donate until your levels are back to normal. Eat iron-rich foods and consult your doctor if needed.";
        } else if (question.contains("diabetes")) {
            return "If you have diabetes, you can donate blood as long as it’s well-managed and you’re not on insulin injections. If you take insulin, you may be deferred. Check with your donation center for their specific rules.";
        } else {
            return "I’m sorry, I don’t have an answer for that. Please contact your local blood donation center for more information.";
        }
    }
}