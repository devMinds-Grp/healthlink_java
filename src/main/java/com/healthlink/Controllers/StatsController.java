package com.healthlink.Controllers;

import com.healthlink.Entites.Reclamation;
import com.healthlink.Services.ReclamationService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class StatsController implements Initializable {

    @FXML private LineChart<String, Number> categoryChart;

    private final ReclamationService reclamationService = new ReclamationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCategoryChart();
     }

    private void setupCategoryChart() {
        // 1. Graphique des réclamations par catégorie (comme avant)
        Map<String, Long> claimsByCategory = reclamationService.getAllReclamations().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategorie() != null ? r.getCategorie().getNom() : "Non catégorisé",
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réclamations par catégorie");
        claimsByCategory.forEach((category, count) ->
                series.getData().add(new XYChart.Data<>(category, count))
        );
        categoryChart.getData().add(series);
    }

 }