package com.healthlink.Controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private WebView webView;
    @FXML private MenuItem patientsMenuItem;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine webEngine = webView.getEngine();
        String powerBiUrl = "https://app.powerbi.com/view?r=eyJrIjoiYzc4ZjcwZTgtMzVlMy00YTY1LWI3NTQtOGY0ZWM0MzA0MzI0IiwidCI6ImRiZDY2NjRkLTRlYjktNDZlYi05OWQ4LTVjNDNiYTE1M2M2MSIsImMiOjl9";
        webEngine.load(powerBiUrl);
    }
    @FXML
    private void showPatientsView() {
        loadView("/views/User/list.fxml");
    }
    // Méthode appelée quand on clique sur "Médecins"
    @FXML
    private void showMedecinsView() {
        loadView("/views/User/listMedecin.fxml");
    }
    // Méthode appelée quand on clique sur "Médecins"
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


    // Méthode pour charger les différentes vues
    private void loadView(String fxmlPath) {
        try {
            // 1. Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Récupérer la fenêtre actuelle
            Stage stage = (Stage) patientsMenuItem.getParentPopup().getOwnerWindow();

            // 3. Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la vue: " + fxmlPath);
        }
    }
}
