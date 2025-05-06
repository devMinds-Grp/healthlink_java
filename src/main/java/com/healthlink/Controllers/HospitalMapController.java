package com.healthlink.Controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HospitalMapController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private HBox inputBox;

    @FXML
    private BorderPane mapPane;

    @FXML
    private TextField latField;

    @FXML
    private TextField lonField;

    @FXML
    private Button findHospitalsButton;

    @FXML
    private Label attributionLabel;

    private MapView mapView;
    private double userLat = 36.862499; // Default: La Petite Ariana, Ariana, Tunisia
    private double userLon = 10.195556;
    private final double searchRadius = 10000; // 10 km radius
    private final List<Marker> hospitalMarkers = new ArrayList<>(); // Track hospital markers

    @FXML
    public void initialize() {
        // Initialize MapView
        mapView = new MapView();
        mapView.setMapType(MapType.OSM);
        mapView.setCenter(new Coordinate(userLat, userLon));
        mapView.setZoom(12);

        // Add user marker
        Marker userMarker = Marker.createProvided(Marker.Provided.RED)
                .setPosition(new Coordinate(userLat, userLon))
                .setVisible(true);
        mapView.addMarker(userMarker);

        // Set MapView in mapPane
        mapPane.setCenter(mapView);

        // Initialize text fields with default coordinates
        latField.setText(String.valueOf(userLat));
        lonField.setText(String.valueOf(userLon));

        // Handle map click to update position
        mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            Coordinate clickedCoord = event.getCoordinate();
            latField.setText(String.valueOf(clickedCoord.getLatitude()));
            lonField.setText(String.valueOf(clickedCoord.getLongitude()));
        });

        // Handle hospital search
        findHospitalsButton.setOnAction(e -> findNearbyHospitals());

        // Initialize map
        mapView.initialize(Configuration.builder().build());
    }

    private void findNearbyHospitals() {
        try {
            userLat = Double.parseDouble(latField.getText());
            userLon = Double.parseDouble(lonField.getText());

            // Clear existing hospital markers
            for (Marker marker : hospitalMarkers) {
                mapView.removeMarker(marker);
            }
            hospitalMarkers.clear();

            // Re-add user marker
            mapView.addMarker(Marker.createProvided(Marker.Provided.RED)
                    .setPosition(new Coordinate(userLat, userLon))
                    .setVisible(true));

            // Query Overpass API for hospitals
            String overpassQuery = "[out:json];" +
                    "node[amenity=hospital](around:" + searchRadius + "," + userLat + "," + userLon + ");" +
                    "out body;";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://overpass-api.de/api/interpreter?data=" + java.net.URLEncoder.encode(overpassQuery, "UTF-8"))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Parse JSON response
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                JsonArray elements = json.getAsJsonArray("elements");

                List<Hospital> hospitals = new ArrayList<>();
                for (JsonElement element : elements) {
                    JsonObject node = element.getAsJsonObject();
                    double lat = node.get("lat").getAsDouble();
                    double lon = node.get("lon").getAsDouble();
                    String name = node.has("tags") && node.getAsJsonObject("tags").has("name") ?
                            node.getAsJsonObject("tags").get("name").getAsString() : "Unknown Hospital";
                    hospitals.add(new Hospital(name, lat, lon));
                }

                // Add hospital markers and calculate distances
                for (Hospital hospital : hospitals) {
                    Marker marker = Marker.createProvided(Marker.Provided.BLUE)
                            .setPosition(new Coordinate(hospital.lat, hospital.lon))
                            .setVisible(true);
                    mapView.addMarker(marker);
                    hospitalMarkers.add(marker); // Track the marker

                    double distance = calculateDistance(userLat, userLon, hospital.lat, hospital.lon);
                    System.out.printf("Hospital: %s, Distance: %.2f km%n", hospital.name, distance);
                }
            }
        } catch (NumberFormatException | IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Haversine formula to calculate distance in kilometers
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Hospital data class
    private static class Hospital {
        String name;
        double lat;
        double lon;

        Hospital(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }
}