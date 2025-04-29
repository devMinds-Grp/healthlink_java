package com.healthlink.Controllers.User;

import com.healthlink.Entites.Utilisateur;
import com.healthlink.Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AffichageSoignant implements Initializable {

    private final UserService userService = new UserService();
    private final ObservableList<Utilisateur> soignantList = FXCollections.observableArrayList();
    @FXML
    private WebView webView;
    // Éléments pour les soignants
    @FXML private TableView<Utilisateur> soignantTableView;
    @FXML private TableColumn<Utilisateur, String> nomSoignantColumn;
    @FXML private TableColumn<Utilisateur, String> prenomSoignantColumn;
    @FXML private TableColumn<Utilisateur, String> emailSoignantColumn;
    @FXML private TableColumn<Utilisateur, String> categorieSoinColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsSoignantColumn;

    @FXML private MenuItem patientsMenuItem;
    @FXML private MenuItem medecinsMenuItem;
    @FXML private MenuItem soignantsMenuItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureSoignantColumns();
        setupActionsColumn();
        loadSoignants();
    }

    private void configureSoignantColumns() {
        nomSoignantColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomSoignantColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailSoignantColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        categorieSoinColumn.setCellValueFactory(new PropertyValueFactory<>("categorie_soin"));
        // Configuration des actions si nécessaire
    }

    private void setupActionsColumn() {
        actionsSoignantColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(btnVoir, btnModifier, btnSupprimer);

            {
                hbox.setSpacing(5);
                btnVoir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnVoir.setOnAction(event -> voirSoignant(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(event -> modifierSoignant(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(event -> supprimerSoignant(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void loadSoignants() {
        soignantList.clear();
        List<Utilisateur> soignants = userService.findAllSoignants();
        soignantList.addAll(soignants);
        soignantTableView.setItems(soignantList);
    }

    @FXML
    private void openAddSoignantForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/ajoutSoignant.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau soignant");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            loadSoignants();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    @FXML
    private void showPatientsView() {
        loadView("/views/User/list.fxml");
    }

    @FXML
    private void showMedecinsView() {
        loadView("/views/User/listMedecin.fxml");
    }

    @FXML
    private void showSoignantView() {
        loadView("/views/User/listSoignant.fxml");
    }

    @FXML
    private void showAttenteView() {
        loadView("/views/User/enattente.fxml");
    }
    @FXML
    public void showDashView(ActionEvent actionEvent) {
        loadView("/views/User/Dashboard.fxml");
    }
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) soignantTableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue: " + fxmlPath);
        }
    }

    private void voirSoignant(Utilisateur soignant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/detailsSoignant.fxml"));
            Parent root = loader.load();

            DetailsSoignant controller = loader.getController();
            controller.initData(soignant);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du Soignant");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails du soignant");
        }
    }

    private void modifierSoignant(Utilisateur soignant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/modifierSoignant.fxml"));
            Parent root = loader.load();

            ModifierSoignant controller = loader.getController();
            controller.initData(soignant);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Soignant");
            stage.showAndWait();

            loadSoignants();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'éditeur de soignant");
        }
    }

    private void supprimerSoignant(Utilisateur soignant) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le soignant");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce soignant?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.deleteSoignant(soignant.getId());
            loadSoignants();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Soignant supprimé avec succès");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void onArrowClicked() {
        // Redirection quand on clique sur la flèche
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) webView.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}