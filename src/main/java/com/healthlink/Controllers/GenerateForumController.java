package com.healthlink.Controllers;

import com.healthlink.Entities.Forum;
import com.healthlink.Services.ForumService;
import com.healthlink.Services.GeminiService;
import com.healthlink.utils.InappropriateWordsFilter;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateForumController {
    @FXML private TextField topicField;
    @FXML private TextArea generatedContentArea;
    @FXML private Button generateButton;
    @FXML private Button acceptButton;
    @FXML private Button rejectButton;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label topicErrorLabel;

    private MainController mainController;
    private final GeminiService geminiService;
    private final ForumService forumService = new ForumService();
    private int currentUserId;
    private String extractedTitle = "";

    public GenerateForumController() {
        geminiService = new GeminiService("AIzaSyA_OXX1irfHQK0ov18MX9rORRdD0IHlOfM");
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
        System.out.println("GenerateForumController - currentUserId défini à : " + currentUserId);
    }

    @FXML
    public void initialize() {
        acceptButton.setDisable(true);
        rejectButton.setDisable(true);
        progressIndicator.setVisible(false);
        topicErrorLabel.setTextFill(Color.RED);
        topicErrorLabel.setVisible(false);

        generateButton.setOnAction(this::handleGenerateClick);
        acceptButton.setOnAction(this::handleAcceptClick);
        rejectButton.setOnAction(this::handleRejectClick);

        setupFieldValidations();
    }

    private void setupFieldValidations() {
        topicField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(topicErrorLabel, "Le sujet est obligatoire");
            } else if (newVal.trim().length() < 5) {
                showFieldError(topicErrorLabel, "Minimum 5 caractères");
            } else if (InappropriateWordsFilter.containsInappropriateWords(newVal)) {
                showFieldError(topicErrorLabel, "Le sujet contient des mots inappropriés");
            } else {
                hideFieldError(topicErrorLabel);
            }
        });
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideFieldError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private boolean validateTopic() {
        String topic = topicField.getText().trim();
        if (topic.isEmpty()) {
            showFieldError(topicErrorLabel, "Le sujet est obligatoire");
            return false;
        } else if (topic.length() < 5) {
            showFieldError(topicErrorLabel, "Minimum 5 caractères");
            return false;
        } else if (InappropriateWordsFilter.containsInappropriateWords(topic)) {
            String inappropriateWord = InappropriateWordsFilter.getFirstInappropriateWord(topic);
            showFieldError(topicErrorLabel, "Le sujet contient le mot inapproprié : " + inappropriateWord);
            return false;
        }
        return true;
    }

    private void handleGenerateClick(ActionEvent event) {
        if (!validateTopic()) {
            return;
        }

        String topic = topicField.getText().trim();
        startGeneration(topic);
    }

    private void startGeneration(String topic) {
        generatedContentArea.setText("");
        statusLabel.setText("Génération en cours...");
        progressIndicator.setVisible(true);
        generateButton.setDisable(true);
        acceptButton.setDisable(true);
        rejectButton.setDisable(true);

        Task<String> generationTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                CompletableFuture<String> future = geminiService.generateForumContent(topic);
                return future.get();
            }
        };

        generationTask.setOnSucceeded(e -> {
            String generatedContent = generationTask.getValue();
            generatedContentArea.setText(generatedContent);

            extractedTitle = extractTitle(generatedContent);
            titleLabel.setText("Titre suggéré : " + extractedTitle);

            generateButton.setDisable(false);
            acceptButton.setDisable(false);
            rejectButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Génération terminée. Vous pouvez accepter ou rejeter ce contenu.");
        });

        generationTask.setOnFailed(e -> {
            Throwable exception = generationTask.getException();
            showAlert("Erreur de génération", "Erreur lors de la génération: " + exception.getMessage());
            generateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Échec de la génération. Veuillez réessayer.");
        });

        new Thread(generationTask).start();
    }

    private String extractTitle(String content) {
        // Priorité 1 : Chercher un titre après #Titre: ou # Titre:
        Pattern titlePattern1 = Pattern.compile("#\\s*Titre\\s*:\\s*(.+?)(?:\\n|$)");
        Matcher matcher1 = titlePattern1.matcher(content);
        if (matcher1.find()) {
            return matcher1.group(1).trim();
        }

        // Priorité 2 : Chercher un titre formaté avec **Titre Principal:** ou # Titre Principal
        Pattern titlePattern2 = Pattern.compile("(?:^|\\n)(?:\\*\\*|#)\\s*([^\\n:*#]{5,100})(?:\\*\\*|\\n|$)");
        Matcher matcher2 = titlePattern2.matcher(content);
        if (matcher2.find()) {
            String potentialTitle = matcher2.group(1).trim();
            // Exclure les sections comme "Introduction:", "Contenu Principal:", "Conclusion:"
            if (!potentialTitle.matches("(?i)^(Introduction|Contenu Principal|Conclusion)\\s*:?$")) {
                return potentialTitle;
            }
        }

        // Priorité 3 : Chercher la première ligne non vide qui semble être un titre
        String[] lines = content.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.matches("(?i)^(Introduction|Contenu Principal|Conclusion)\\s*:?$")) {
                return line.replaceAll("^[\\*\\#\\s]+", "").replaceAll("[\\*\\s]+$", "");
            }
        }

        // Dernier recours : Utiliser le sujet saisi
        return "Forum sur " + topicField.getText().trim();
    }

    private boolean validateForumData(String title, String content) {
        List<String> errors = new ArrayList<>();

        if (title == null || title.trim().isEmpty()) {
            errors.add("Le titre est obligatoire");
        } else if (title.trim().length() < 5) {
            errors.add("Le titre doit contenir au moins 5 caractères");
        } else if (title.length() > 255) {
            errors.add("Le titre est trop long (maximum 255 caractères)");
        }
        if (InappropriateWordsFilter.containsInappropriateWords(title)) {
            errors.add("Le titre contient des mots inappropriés : " + InappropriateWordsFilter.getFirstInappropriateWord(title));
        }

        if (content == null || content.trim().isEmpty()) {
            errors.add("La description est obligatoire");
        } else if (content.trim().length() < 10) {
            errors.add("La description doit contenir au moins 10 caractères");
        }
        if (InappropriateWordsFilter.containsInappropriateWords(content)) {
            errors.add("La description contient des mots inappropriés : " + InappropriateWordsFilter.getFirstInappropriateWord(content));
        }

        if (!errors.isEmpty()) {
            showAlert("Erreur de validation", String.join("\n", errors));
            return false;
        }

        return true;
    }

    private void handleAcceptClick(ActionEvent event) {
        if (generatedContentArea.getText().isEmpty()) {
            showAlert("Attention", "Aucun contenu n'a été généré");
            return;
        }

        if (currentUserId <= 0) {
            showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter pour créer un forum.");
            return;
        }

        String content = generatedContentArea.getText().trim();
        String title = extractedTitle.trim();

        if (!validateForumData(title, content)) {
            return;
        }

        Forum forum = new Forum(
                title,
                content,
                new Date(),
                currentUserId,
                false
        );

        try {
            System.out.println("Tentative d'enregistrement du forum: " + forum);
            forumService.add(forum);
            showAlert("Succès", "Forum créé avec succès ! Il sera visible après approbation par un administrateur.");
            showForumList();
        } catch (RuntimeException e) {
            String errorMessage = "Échec de la création du forum : " + e.getMessage();
            if (e.getMessage().contains("foreign key constraint")) {
                errorMessage += "\nVérifiez que l'utilisateur avec l'ID " + currentUserId + " existe dans la table utilisateur.";
            }
            showAlert("Erreur", errorMessage);
            e.printStackTrace();
        }
    }

    private void handleRejectClick(ActionEvent event) {
        generatedContentArea.clear();
        extractedTitle = "";
        titleLabel.setText("");
        statusLabel.setText("Générez un nouveau contenu de forum");
        acceptButton.setDisable(true);
        rejectButton.setDisable(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showForumList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ForumList.fxml"));
            Parent view = loader.load();
            ForumListController controller = loader.getController();
            controller.setMainController(mainController);

            StackPane mainContainer = (StackPane) topicField.getScene().lookup("#mainContainer");
            if (mainContainer != null) {
                mainContainer.getChildren().setAll(view);
            } else {
                StackPane parentContainer = (StackPane) topicField.getScene().getRoot().lookup(".mainContainer");
                if (parentContainer != null) {
                    parentContainer.getChildren().setAll(view);
                } else {
                    System.err.println("mainContainer non trouvé. Recherche alternative...");
                    StackPane rootContainer = (StackPane) topicField.getScene().getRoot();
                    if (rootContainer != null) {
                        rootContainer.getChildren().setAll(view);
                    } else {
                        showAlert("Erreur", "Impossible de charger la liste des forums : conteneur principal non trouvé.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des forums : " + e.getMessage());
        }
    }
}