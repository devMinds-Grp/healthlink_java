package com.healthlink.Controllers;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Entities.ChatMessage;
import com.healthlink.Entities.Notification;
import com.healthlink.Services.AuthService;
import com.healthlink.Services.ChatService;
import com.healthlink.Services.NotificationService;
import com.healthlink.Services.SMSService;
import com.healthlink.utils.MyDB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Alert;

public class NavbarController {

    @FXML private Button homeButton;
    @FXML private Button careButton;
    @FXML private Button prescriptionButton;
    @FXML private Button appointmentButton;
    @FXML private Button doctorButton;
    @FXML private Button myAppointmentsButton;
    @FXML private Button donButton;
    @FXML private Button donationResponseButton;
    @FXML private Button hospitalsButton;
    @FXML private Button ForumButton;
    @FXML private Button ReclamationButton;
    @FXML private Button profileButton;
    @FXML private Button disconnectButton;
    @FXML private Button dashboardButton;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    private final NotificationService notificationService;
    private final ChatService chatService;
    private Utilisateur currentUser;
    private Stage notificationStage;
    private long lastUnreadCount = -1;
    private Timeline badgeTimeline;

    public NavbarController() {
        try {
            this.notificationService = new NotificationService(MyDB.getInstance().getConnection());
            this.chatService = new ChatService();
            System.out.println("NavbarController initialized with NotificationService and ChatService");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    // Map to store navigation details
    private static final Map<String, NavigationTarget> NAVIGATION_MAP = new HashMap<>();

    static {
        NAVIGATION_MAP.put("home", new NavigationTarget("/views/Home.fxml", "Accueil"));
        NAVIGATION_MAP.put("care", new NavigationTarget("/views/care/ListCare.fxml", "Liste des Soins"));
        NAVIGATION_MAP.put("prescription", new NavigationTarget("/views/Prescription/list_prescription.fxml", "Liste des Prescriptions"));
        NAVIGATION_MAP.put("appointment", new NavigationTarget("/views/Appointment/list_appointments.fxml", "Liste des Rendez-vous"));
        NAVIGATION_MAP.put("doctors", new NavigationTarget("/views/Appointment/list_doctor.fxml", "Liste des Médecins"));
        NAVIGATION_MAP.put("myAppointments", new NavigationTarget("/views/Appointment/my_appointments.fxml", "Mes Rendez-vous"));
        NAVIGATION_MAP.put("don", new NavigationTarget("/views/list_don.fxml", "Donation"));
        NAVIGATION_MAP.put("donationResponse", new NavigationTarget("/views/list_reponse_don.fxml", "Donation Response List"));
        NAVIGATION_MAP.put("hospitals", new NavigationTarget("/views/map_view.fxml", "Find Nearby Hospitals"));
        NAVIGATION_MAP.put("forum", new NavigationTarget("/views/MainView.fxml", "Forum"));
        NAVIGATION_MAP.put("reclamations", new NavigationTarget("/list_reclamations.fxml", "Reclamations"));
        NAVIGATION_MAP.put("dashboard", new NavigationTarget("/views/User/list.fxml", "Tableau de bord"));
        NAVIGATION_MAP.put("profile", new NavigationTarget("/views/User/Profile.fxml", "Profil"));
        NAVIGATION_MAP.put("disconnect", new NavigationTarget("/views/User/Auth/Login.fxml", "Connexion"));
    }

    // Static class to hold navigation target details
    private static class NavigationTarget {
        String fxmlPath;
        String title;

        NavigationTarget(String fxmlPath, String title) {
            this.fxmlPath = fxmlPath;
            this.title = title;
        }
    }

    @FXML
    public void initialize() {
        currentUser = AuthService.getConnectedUtilisateur();
        Utilisateur utilisateur = AuthService.getConnectedUtilisateur();
        Button[] allButtons = {
                homeButton, careButton, prescriptionButton, appointmentButton, doctorButton,
                myAppointmentsButton, donButton, donationResponseButton, hospitalsButton,
                ForumButton, ReclamationButton, profileButton, disconnectButton, dashboardButton
        };

        // Hide all buttons by default
        setButtonVisibility(false, allButtons);

        // If no user is logged in, hide all buttons and return
        if (utilisateur == null) {
            setButtonVisibility(false, homeButton, careButton, prescriptionButton, appointmentButton,
                    donButton, ForumButton, ReclamationButton, profileButton, disconnectButton,
                    dashboardButton);
            return;
        }

        // Notification setup for users with role ID > 2
        if (currentUser != null && currentUser.getRole().getId() > 2) {
            System.out.println("Current user: ID=" + currentUser.getId() + ", Role=" + currentUser.getRole().getId());
            updateNotificationBadge();

            if (notificationIcon == null) {
                System.err.println("ERROR: notificationIcon is null - Check fx:id in Navbar.fxml");
            } else {
                System.out.println("notificationIcon initialized successfully");
                notificationIcon.setStyle("-fx-background-color: yellow; -fx-border-color: red; -fx-border-width: 2;");
                notificationIcon.setPickOnBounds(true);

                notificationIcon.setOnMouseClicked(event -> {
                    System.out.println("Notification icon clicked for user ID=" + currentUser.getId());
                    if (notificationStage != null && notificationStage.isShowing()) {
                        System.out.println("Closing notification stage");
                        notificationStage.close();
                    } else {
                        showNotifications();
                    }
                });
                System.out.println("Click handler set on notificationIcon");
            }

            notificationIcon.setVisible(true);
            notificationBadge.setVisible(true);

            badgeTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateNotificationBadge()));
            badgeTimeline.setCycleCount(Timeline.INDEFINITE);
            badgeTimeline.play();
        } else {
            notificationIcon.setVisible(false);
            notificationBadge.setVisible(false);
        }

        // Show buttons based on user role
        switch (utilisateur.getRole().getId()) {
            case 1: // Admin
                setButtonVisibility(true, homeButton, dashboardButton, prescriptionButton,
                        ForumButton, profileButton, ReclamationButton, donButton, disconnectButton);
                break;
            case 2: // Doctor
                setButtonVisibility(true, homeButton, prescriptionButton, myAppointmentsButton,
                        hospitalsButton, ForumButton, ReclamationButton, profileButton, disconnectButton);
                break;
            case 3: // Patient
                setButtonVisibility(true, homeButton, appointmentButton, doctorButton, donButton,
                        donationResponseButton, careButton, hospitalsButton, ForumButton, ReclamationButton,
                        profileButton, disconnectButton);
                break;
            case 4: // Soignant
                setButtonVisibility(true, homeButton, careButton, hospitalsButton,
                        ForumButton, ReclamationButton, profileButton, disconnectButton);
                break;
            default:
                // No buttons visible for unrecognized roles
                break;
        }
    }

    private void updateNotificationBadge() {
        try {
            List<Notification> notifications = notificationService.getNotificationsByUserId(currentUser.getId());
            long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
            if (unreadCount != lastUnreadCount) {
                System.out.println("Fetched " + notifications.size() + " notifications for user ID=" + currentUser.getId());
                System.out.println("Unread notifications: " + unreadCount);
                lastUnreadCount = unreadCount;
                if (unreadCount > 0) {
                    notificationBadge.setText(String.valueOf(unreadCount));
                    notificationBadge.setVisible(true);
                } else {
                    notificationBadge.setVisible(false);
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating notification badge: " + e.getMessage());
            notificationBadge.setVisible(false);
        }
    }

    private void showNotifications() {
        try {
            System.out.println("Showing notifications for user ID=" + currentUser.getId());
            List<Notification> notifications = notificationService.getNotificationsByUserId(currentUser.getId());
            System.out.println("Notifications fetched: " + notifications.size());
            if (notifications.isEmpty()) {
                System.out.println("No notifications found");
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune notification disponible.");
                return;
            }

            VBox notificationPane = new VBox(10);
            notificationPane.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; " +
                    "-fx-background-radius: 5; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

            for (Notification notification : notifications) {
                addNotificationToPane(notificationPane, notification);
            }

            notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.UNDECORATED);
            Scene scene = new Scene(notificationPane, 400, Math.min(notifications.size() * 60, 300));
            try {
                scene.getStylesheets().add(getClass().getResource("/css/Care.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Failed to load Care.css: " + e.getMessage());
            }
            notificationStage.setScene(scene);

            double iconX = notificationIcon.localToScreen(notificationIcon.getBoundsInLocal()).getMinX();
            double iconY = notificationIcon.localToScreen(notificationIcon.getBoundsInLocal()).getMaxY();
            System.out.println("Positioning stage at X=" + (iconX - 350) + ", Y=" + (iconY + 5));
            notificationStage.setX(iconX - 350);
            notificationStage.setY(iconY + 5);

            notificationStage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    System.out.println("Stage lost focus, closing");
                    notificationStage.close();
                }
            });

            Timeline notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                List<Notification> updatedNotifications = notificationService.getNotificationsByUserId(currentUser.getId());
                long unreadCount = updatedNotifications.stream().filter(n -> !n.isRead()).count();
                if (unreadCount != lastUnreadCount) {
                    System.out.println("Notifications updated: " + updatedNotifications.size() + ", Unread: " + unreadCount);
                    notificationPane.getChildren().clear();
                    if (updatedNotifications.isEmpty()) {
                        notificationStage.close();
                    } else {
                        updatedNotifications.forEach(n -> addNotificationToPane(notificationPane, n));
                    }
                    updateNotificationBadge();
                }
            }));
            notificationTimeline.setCycleCount(Timeline.INDEFINITE);
            notificationTimeline.play();
            notificationStage.setOnHidden(e -> {
                notificationTimeline.stop();
                System.out.println("Notification polling stopped");
            });

            System.out.println("Showing notification stage");
            notificationStage.show();
        } catch (Exception e) {
            System.err.println("Error in showNotifications: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to show notifications: " + e.getMessage());
        }
    }

    private void addNotificationToPane(VBox notificationPane, Notification notification) {
        HBox notificationBox = new HBox(10);
        notificationBox.setStyle("-fx-padding: 5; -fx-alignment: center-left;");

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setStyle(notification.isRead() ?
                "-fx-font-size: 12px; -fx-text-fill: #333333;" :
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        Button viewChatButton = new Button("View Chat");
        viewChatButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 10px;");
        viewChatButton.setOnAction(event -> {
            int patientId = extractPatientIdFromMessage(notification.getMessage());
            if (patientId != -1) {
                System.out.println("Opening chat with patient ID=" + patientId);
                openChatWithPatient(patientId);
                notificationService.markAsRead(notification.getId());
                updateNotificationBadge();
                notificationPane.getChildren().clear();
                List<Notification> updatedNotifications = notificationService.getNotificationsByUserId(currentUser.getId());
                if (updatedNotifications.isEmpty()) {
                    notificationStage.close();
                } else {
                    updatedNotifications.forEach(n -> addNotificationToPane(notificationPane, n));
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to open chat: Patient ID could not be extracted.");
            }
        });

        // Handle SMS reply if message indicates an SMS response
        if (notification.getMessage().startsWith("SMS Reply: ")) {
            int patientId = extractPatientIdFromMessage(notification.getMessage().replace("SMS Reply: ", ""));
            if (patientId != -1) {
                ChatMessage chatMessage = new ChatMessage(patientId, currentUser.getId(), notification.getMessage().replace("SMS Reply: ", ""));
                chatService.sendMessage(chatMessage);
                notificationService.markAsRead(notification.getId());
            }
        }

        notificationBox.getChildren().addAll(messageLabel, viewChatButton);
        notificationPane.getChildren().add(notificationBox);
    }

    private int extractPatientIdFromMessage(String message) {
        try {
            System.out.println("Extracting patient ID from message: " + message);
            String prefix = "New message from patient (ID: ";
            int startIndex = message.indexOf(prefix);
            if (startIndex == -1) {
                System.err.println("Prefix not found in message: " + message);
                return -1;
            }
            startIndex += prefix.length();
            int endIndex = message.indexOf(")", startIndex);
            if (endIndex == -1) {
                System.err.println("Closing parenthesis not found in message: " + message);
                return -1;
            }
            String idStr = message.substring(startIndex, endIndex);
            System.out.println("Extracted ID string: " + idStr);
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse patient ID: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.err.println("Failed to extract patient ID from message: " + message + ", error: " + e.getMessage());
            return -1;
        }
    }

    private void openChatWithPatient(int patientUserId) {
        System.out.println("openChatWithPatient called with patientUserId=" + patientUserId);
        try {
            Stage chatStage = new Stage();
            chatStage.initStyle(StageStyle.DECORATED);
            chatStage.setTitle("Chat with Patient");

            // Main chat container with gradient background
            VBox chatPane = new VBox(10);
            chatPane.setStyle("-fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #f0f4f8, #ffffff);");

            // Chat messages area
            ListView<ChatMessage> chatMessages = new ListView<>();
            chatMessages.setPrefHeight(400);
            chatMessages.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            // Custom cell factory for message bubbles
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

                        // Style based on sender
                        boolean isSender = message.getSenderId() == currentUser.getId();
                        messageLabel.getStyleClass().add(isSender ? "message-bubble-sender" : "message-bubble-receiver");
                        messageBox.setAlignment(isSender ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

                        messageBox.getChildren().add(messageLabel);
                        setGraphic(messageBox);
                    }
                }
            });

            // Load initial messages
            loadMessages(chatMessages, patientUserId);

            // Auto-scroll to the latest message
            chatMessages.getItems().addListener((javafx.collections.ListChangeListener<ChatMessage>) change -> {
                chatMessages.scrollTo(chatMessages.getItems().size() - 1);
            });

            // Polling for new messages
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> loadMessages(chatMessages, patientUserId)));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            chatStage.setOnHidden(e -> {
                timeline.stop();
                System.out.println("Chat message polling stopped for patientUserId=" + patientUserId);
            });

            // Input area
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
                System.out.println("Send button clicked");
                String messageContent = messageField.getText();
                if (!messageContent.trim().isEmpty()) {
                    System.out.println("Sending message: " + messageContent);
                    ChatMessage chatMessage = new ChatMessage(currentUser.getId(), patientUserId, messageContent);
                    chatService.sendMessage(chatMessage);
                    chatMessages.getItems().add(chatMessage);
                    messageField.clear();
                } else {
                    System.out.println("Message content is empty, not sending");
                }
            });

            notifySMSButton.setOnAction(event -> {
                System.out.println("Notify via SMS button clicked for patientUserId=" + patientUserId);
                sendMessageNotificationSMS(patientUserId);
            });

            inputBox.getChildren().addAll(messageField, sendButton, notifySMSButton);
            chatPane.getChildren().addAll(chatMessages, inputBox);

            Scene scene = new Scene(chatPane, 450, 500);
            scene.getStylesheets().add(getClass().getResource("/css/Chat.css").toExternalForm());
            chatStage.setScene(scene);
            chatStage.show();
        } catch (Exception e) {
            System.err.println("Error opening chat: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open chat: " + e.getMessage());
        }
    }

    private void sendMessageNotificationSMS(int patientUserId) {
        try {
            Utilisateur patient = chatService.getUserById(patientUserId);
            if (patient == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Patient information is not available.");
                return;
            }

            System.out.println("Patient ID: " + patient.getId() + ", Name: " + patient.getPrenom() + " " + patient.getNom());
            System.out.println("Patient object: num_tel = " + patient.getNum_tel() + ", email = " + patient.getEmail());
            long phoneNumber = patient.getNum_tel();
            System.out.println("Retrieved phone number for patient ID " + patient.getId() + ": " + phoneNumber);

            if (phoneNumber == 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "No phone number available for patient. Please ensure the patient's phone number is set correctly in the database.");
                return;
            }

            String phoneNumberStr = String.valueOf(phoneNumber);
            System.out.println("Phone number as string: " + phoneNumberStr);
            if (phoneNumberStr.length() != 8) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid phone number format. Tunisian numbers should be 8 digits long (without country code). Found: " + phoneNumberStr);
                return;
            }

            String formattedPhoneNumber = "+216" + phoneNumberStr;
            System.out.println("Formatted phone number for SMS: " + formattedPhoneNumber);

            String senderName = (currentUser.getPrenom() != null ? currentUser.getPrenom() : "") + " " +
                    (currentUser.getNom() != null ? currentUser.getNom() : "");
            senderName = senderName.trim().isEmpty() ? "Anonymous" : senderName.trim();
            String receiverName = (patient.getPrenom() != null ? patient.getPrenom() : "") + " " +
                    (patient.getNom() != null ? patient.getNom() : "");
            receiverName = receiverName.trim().isEmpty() ? "Friend" : receiverName.trim();
            String message = "Hey Mr " + receiverName + ", You have received a new message from " + senderName + ". Please check your app.";
            SMSService.sendSMS(formattedPhoneNumber, message);

            showAlert(Alert.AlertType.INFORMATION, "Success", "SMS notification sent to patient successfully.");
        } catch (Exception e) {
            System.err.println("Failed to send SMS notification: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to send SMS notification: " + e.getMessage());
        }
    }

    private void loadMessages(ListView<ChatMessage> chatMessages, int otherUserId) {
        try {
            List<ChatMessage> messages = chatService.getMessages(currentUser.getId(), otherUserId);
            if (messages.size() != chatMessages.getItems().size()) {
                System.out.println("loadMessages called with otherUserId=" + otherUserId);
                System.out.println("Retrieved " + messages.size() + " messages");
                chatMessages.getItems().setAll(messages);
            }
        } catch (Exception e) {
            System.err.println("Error loading messages: " + e.getMessage());
        }
    }

    // Utility method to set button visibility and managed property
    private void setButtonVisibility(boolean visible, Button... buttons) {
        for (Button button : buttons) {
            if (button != null) {
                button.setVisible(visible);
                button.setManaged(visible);
            }
        }
    }

    // Centralized navigation method
    private void navigateTo(String targetKey) {
        NavigationTarget target = NAVIGATION_MAP.get(targetKey);
        if (target == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cible de navigation inconnue.");
            return;
        }
        navigateTo(target.fxmlPath, target.title);

        // Handle logout for disconnect action
        if ("disconnect".equals(targetKey)) {
            AuthService.logout();
        }
    }

    // Helper method to load FXML and set stage
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) homeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            stage.setWidth(screenSize.getWidth());
            stage.setHeight(screenSize.getHeight());
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // Navigation methods
    @FXML private void navigateToHome() { navigateTo("home"); }
    @FXML private void navigateToCare() { navigateTo("care"); }
    @FXML private void navigateToPrescription() { navigateTo("prescription"); }
    @FXML private void navigateToAppointment() { navigateTo("appointment"); }
    @FXML private void navigateToDoctors() { navigateTo("doctors"); }
    @FXML private void navigateToMyAppointments() { navigateTo("myAppointments"); }
    @FXML private void navigateToDon() { navigateTo("don"); }
    @FXML private void navigateToDonationResponse() { navigateTo("donationResponse"); }
    @FXML private void navigateToHospitals() { navigateTo("hospitals"); }
    @FXML private void navigateToForum() { navigateTo("forum"); }
    @FXML private void navigateToReclamations() { navigateTo("reclamations"); }
    @FXML private void navigateToDashboard() { navigateTo("dashboard"); }
    @FXML private void navigateToProfile() { navigateTo("profile"); }
    @FXML private void navigateToDisconnect() {
        if (badgeTimeline != null) {
            badgeTimeline.stop();
            System.out.println("Badge polling stopped on disconnect");
        }
        AuthService.logout();
        navigateTo("disconnect");
    }

    // Utility method to show alerts with customizable type
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}