package com.healthlink.Controllers.CareRespController;

import com.healthlink.Entities.CareResponse;
import com.healthlink.Entities.ChatMessage;
import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.AuthService;
import com.healthlink.Services.CareResponseService;
import com.healthlink.Services.ChatService;
import com.healthlink.Services.SMSService;
import com.healthlink.utils.MyDB;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ListCareResponsesController {

    @FXML private TableView<CareResponse> responseTable;
    @FXML private TableColumn<CareResponse, Void> ownerColumn;
    @FXML private TableColumn<CareResponse, Void> actionsColumn;

    private final CareResponseService responseService;
    private final ChatService chatService;
    private int careId;
    private Utilisateur currentUser;
    private boolean isInitialized = false;
    private final String instanceId = UUID.randomUUID().toString();
    private int lastMessageCount = -1;

    public ListCareResponsesController() {
        try {
            Connection connection = MyDB.getInstance().getConnection();
            this.responseService = new CareResponseService(connection);
            this.chatService = new ChatService();
            System.out.println("ListCareResponsesController instance created: " + instanceId);
        } catch (SQLException e) {
            System.err.println("Failed to initialize ListCareResponsesController: " + e.getMessage());
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing ListCareResponsesController instance: " + instanceId);
        if (responseTable == null || ownerColumn == null || actionsColumn == null) {
            System.err.println("ERROR: FXML fields not injected for instance " + instanceId + ". This controller must be loaded via FXML.");
            throw new IllegalStateException("ListCareResponsesController must be loaded via FXML. FXML fields are null.");
        }

        try {
            // Configure Owner column
            ownerColumn.setCellFactory(param -> new TableCell<CareResponse, Void>() {
                private final HBox container = new HBox(10);
                private final ImageView avatar = new ImageView();
                private final VBox textBox = new VBox(2);
                private final Label nameLabel = new Label();
                private final Label dateLabel = new Label();

                {
                    container.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e9ecef; -fx-border-width: 1;");
                    avatar.setFitWidth(40);
                    avatar.setFitHeight(40);
                    avatar.setStyle("-fx-background-radius: 50%;");
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
                    dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
                    container.getChildren().addAll(avatar, textBox);
                    textBox.getChildren().addAll(nameLabel, dateLabel);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        CareResponse response = getTableRow().getItem();
                        Utilisateur soignant = response.getSoignant();
                        System.out.println("Response ID: " + response.getId() + ", Soignant: " +
                                (soignant != null ? "ID=" + soignant.getId() + ", Role=" + soignant.getRole().getId() : "null"));

                        String prenom = soignant != null && soignant.getPrenom() != null ? soignant.getPrenom() : "";
                        String nom = soignant != null && soignant.getNom() != null ? soignant.getNom() : "";
                        String fullName = (!prenom.isEmpty() || !nom.isEmpty()) ? (prenom + " " + nom).trim() : "Anonymous";
                        System.out.println("FullName: " + fullName + ", Prenom: " + prenom + ", Nom: " + nom);
                        nameLabel.setText(fullName);

                        dateLabel.setText(response.getDateRep() != null ?
                                response.getDateRep().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");

                        String imagePath = soignant != null && soignant.getImageprofile() != null && !soignant.getImageprofile().isEmpty() ?
                                "file:C:/Users/ghass/Desktop/PIJAVA/healthlink_java/profile_images/" + soignant.getImageprofile() : null;
                        System.out.println("Image Path: " + (imagePath != null ? imagePath : "null"));
                        try {
                            if (imagePath != null) {
                                Image image = new Image(imagePath, true);
                                avatar.setImage(image);
                            } else {
                                avatar.setImage(new Image("file:src/main/resources/img/avatar.png"));
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to load profile image: " + (imagePath != null ? imagePath : "null") + ", error: " + e.getMessage());
                            avatar.setImage(new Image("file:src/main/resources/img/avatar.png"));
                        }

                        setGraphic(container);
                    }
                }
            });

            // Configure Actions column
            actionsColumn.setCellFactory(param -> new TableCell<CareResponse, Void>() {
                private final Button deleteButton = new Button("Delete");
                private final Button updateButton = new Button("Update");
                private final Button chatButton = new Button("Chat");
                private final HBox buttonBox = new HBox(5);

                {
                    deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");
                    updateButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;");
                    chatButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5;");
                    deleteButton.setOnAction(event -> {
                        CareResponse response = getTableView().getItems().get(getIndex());
                        if (response != null) {
                            handleDelete(response);
                        }
                    });
                    updateButton.setOnAction(event -> {
                        CareResponse response = getTableView().getItems().get(getIndex());
                        if (response != null) {
                            goToUpdateCareResponse(response);
                        }
                    });
                    chatButton.setOnAction(event -> {
                        CareResponse response = getTableView().getItems().get(getIndex());
                        if (response != null && response.getSoignant() != null) {
                            if (response.getSoignant().getRole().getId() == 4) {
                                openChatWithSoignant(response.getSoignant().getId());
                            } else {
                                showAlert("Error", "This response is not associated with a soignant (Role ID 4).");
                            }
                        } else {
                            showAlert("Error", "Soignant information is missing for this response.");
                        }
                    });

                    if (currentUser != null && currentUser.getRole().getId() == 3) {
                        buttonBox.getChildren().addAll(deleteButton, updateButton, chatButton);
                    } else {
                        buttonBox.getChildren().addAll(deleteButton, updateButton);
                    }
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttonBox);
                }
            });

            isInitialized = true;
            System.out.println("ListCareResponsesController instance " + instanceId + " initialization complete.");
        } catch (Exception e) {
            System.err.println("Failed to initialize response table for instance " + instanceId + ": " + e.getMessage());
            showAlert("Error", "Failed to initialize response table: " + e.getMessage());
        }
    }

    public void setCareId(int careId) {
        this.careId = careId;
        this.currentUser = AuthService.getConnectedUtilisateur();
        loadResponses();
    }

    public void setupNotificationPolling(Stage stage) {
        // No longer needed, as notifications are not displayed in this controller
    }

    private void loadResponses() {
        try {
            List<CareResponse> responses = responseService.getResponsesByCareId(careId);
            responseTable.getItems().setAll(responses);
            if (responses.isEmpty()) {
                showAlert("Information", "No responses found for this care record.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load responses for instance " + instanceId + ": " + e.getMessage());
            showAlert("Error", "Failed to load responses: " + e.getMessage());
        }
    }

    private void handleDelete(CareResponse response) {
        try {
            boolean success = responseService.deleteCareResponse(response.getId());
            if (success) {
                showAlert("Success", "Care response deleted successfully");
                loadResponses();
            } else {
                showAlert("Error", "Failed to delete care response");
            }
        } catch (Exception e) {
            System.err.println("Failed to delete response for instance " + instanceId + ": " + e.getMessage());
            showAlert("Error", "Failed to delete care response: " + e.getMessage());
        }
    }

    private void goToUpdateCareResponse(CareResponse response) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/careResp/UpdateCareResponse.fxml"));
            Parent root = loader.load();
            UpdateCareResponseController controller = loader.getController();
            controller.setCareResponse(response);
            Stage stage = new Stage();
            stage.setTitle("Update Care Response");
            stage.setScene(new Scene(root, 600, 400));
            stage.setOnHidden(event -> loadResponses());
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load UpdateCareResponse.fxml for instance " + instanceId + ": " + e.getMessage());
            showAlert("Error", "Failed to open Update Care Response form: " + e.getMessage());
        }
    }

    private void openChatWithSoignant(int soignantUserId) {
        System.out.println("openChatWithSoignant called with soignantUserId=" + soignantUserId + " for instance " + instanceId);
        openChat(soignantUserId, "Chat with Soignant");
    }

    private void openChat(int otherUserId, String title) {
        System.out.println("openChat called with otherUserId=" + otherUserId + ", title=" + title + " for instance " + instanceId);
        try {
            Stage chatStage = new Stage();
            chatStage.initStyle(StageStyle.DECORATED);
            chatStage.setTitle(title);

            VBox chatPane = new VBox(10);
            chatPane.setStyle("-fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #f0f4f8, #ffffff);");

            ListView<ChatMessage> chatMessages = new ListView<>();
            chatMessages.setPrefHeight(400);
            chatMessages.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            chatMessages.setCellFactory(listView -> new ListCell<ChatMessage>() {
                @Override
                protected void updateItem(ChatMessage message, boolean empty) {
                    super.updateItem(message, empty);
                    if (empty || message == null) {
                        setGraphic(null);
                    } else {
                        HBox messageBox = new HBox();
                        messageBox.setSpacing(10);
                        messageBox.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

                        Label messageLabel = new Label(message.getMessage());
                        messageLabel.setWrapText(true);
                        messageLabel.setMaxWidth(250);
                        messageLabel.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));

                        boolean isSender = message.getSenderId() == currentUser.getId();
                        messageLabel.getStyleClass().add(isSender ? "message-bubble-sender" : "message-bubble-receiver");
                        messageBox.setAlignment(isSender ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

                        messageBox.getChildren().add(messageLabel);
                        setGraphic(messageBox);
                    }
                }
            });

            loadMessages(chatMessages, otherUserId);

            chatMessages.getItems().addListener((javafx.collections.ListChangeListener<ChatMessage>) change -> {
                chatMessages.scrollTo(chatMessages.getItems().size() - 1);
            });

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> loadMessages(chatMessages, otherUserId)));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            chatStage.setOnHidden(e -> {
                timeline.stop();
                System.out.println("Chat message polling stopped for otherUserId=" + otherUserId + " in instance " + instanceId);
            });

            HBox inputBox = new HBox(10);
            inputBox.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-background-radius: 20; -fx-border-radius: 20; " +
                    "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

            TextField messageField = new TextField();
            messageField.setPromptText("Type your message...");
            messageField.getStyleClass().add("chat-input");
            HBox.setHgrow(messageField, javafx.scene.layout.Priority.ALWAYS);

            Button sendButton = new Button();
            Image sendIcon = new Image(getClass().getResourceAsStream("/img/send.png"));
            ImageView sendIconView = new ImageView(sendIcon);
            sendIconView.setFitWidth(20);
            sendIconView.setFitHeight(20);
            sendButton.setGraphic(sendIconView);
            sendButton.getStyleClass().add("send-button");

            Button notifySMSButton = new Button("Notify via SMS");
            notifySMSButton.getStyleClass().add("notify-sms-button");

            sendButton.setOnAction(event -> {
                System.out.println("Send button clicked for instance " + instanceId);
                String messageContent = messageField.getText();
                if (!messageContent.trim().isEmpty()) {
                    System.out.println("Sending message: " + messageContent + " for instance " + instanceId);
                    ChatMessage chatMessage = new ChatMessage(currentUser.getId(), otherUserId, messageContent);
                    chatService.sendMessage(chatMessage);
                    chatMessages.getItems().add(chatMessage);
                    messageField.clear();
                } else {
                    System.out.println("Message content is empty, not sending for instance " + instanceId);
                }
            });

            notifySMSButton.setOnAction(event -> {
                System.out.println("Notify via SMS button clicked for otherUserId=" + otherUserId);
                sendMessageNotificationSMS(otherUserId);
            });

            inputBox.getChildren().addAll(messageField, sendButton, notifySMSButton);
            chatPane.getChildren().addAll(chatMessages, inputBox);

            Scene scene = new Scene(chatPane, 450, 500);
            scene.getStylesheets().add(getClass().getResource("/css/Chat.css").toExternalForm());
            chatStage.setScene(scene);
            chatStage.show();
        } catch (Exception e) {
            System.err.println("Error opening chat for instance " + instanceId + ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open chat: " + e.getMessage());
        }
    }

    private void sendMessageNotificationSMS(int otherUserId) {
        try {
            Utilisateur user = chatService.getUserById(otherUserId);
            if (user == null) {
                showAlert("Error", "User information is not available.");
                return;
            }

            System.out.println("User ID: " + user.getId() + ", Name: " + user.getPrenom() + " " + user.getNom());
            System.out.println("User object: num_tel = " + user.getNum_tel() + ", email = " + user.getEmail());
            long phoneNumber = user.getNum_tel();
            System.out.println("Retrieved phone number for user ID " + user.getId() + ": " + phoneNumber);

            if (phoneNumber == 0) {
                showAlert("Error", "No phone number available for user. Please ensure the user's phone number is set correctly in the database.");
                return;
            }

            String phoneNumberStr = String.valueOf(phoneNumber);
            System.out.println("Phone number as string: " + phoneNumberStr);
            if (phoneNumberStr.length() != 8) {
                showAlert("Error", "Invalid phone number format. Tunisian numbers should be 8 digits long (without country code). Found: " + phoneNumberStr);
                return;
            }

            String formattedPhoneNumber = "+216" + phoneNumberStr;
            System.out.println("Formatted phone number for SMS: " + formattedPhoneNumber);

            String senderName = (currentUser.getPrenom() != null ? currentUser.getPrenom() : "") + " " +
                    (currentUser.getNom() != null ? currentUser.getNom() : "");
            senderName = senderName.trim().isEmpty() ? "Anonymous" : senderName.trim();
            String receiverName = (user.getPrenom() != null ? user.getPrenom() : "") + " " +
                    (user.getNom() != null ? user.getNom() : "");
            receiverName = receiverName.trim().isEmpty() ? "Friend" : receiverName.trim();
            String message = "Hey Mr " + receiverName + ", You have received a new message from " + senderName + ". Please check your app.";
            SMSService.sendSMS(formattedPhoneNumber, message);

            showAlert("Success", "SMS notification sent to user successfully.");
        } catch (Exception e) {
            System.err.println("Failed to send SMS notification: " + e.getMessage());
            showAlert("Error", "Failed to send SMS notification: " + e.getMessage());
        }
    }

    private void loadMessages(ListView<ChatMessage> chatMessages, int otherUserId) {
        try {
            List<ChatMessage> messages = chatService.getMessages(currentUser.getId(), otherUserId);
            if (messages.size() != lastMessageCount) {
                System.out.println("loadMessages called with otherUserId=" + otherUserId + " for instance " + instanceId);
                System.out.println("Retrieved " + messages.size() + " messages for instance " + instanceId);
                lastMessageCount = messages.size();
                chatMessages.getItems().setAll(messages);
            }
        } catch (Exception e) {
            System.err.println("Error loading messages for instance " + instanceId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        responseTable.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}