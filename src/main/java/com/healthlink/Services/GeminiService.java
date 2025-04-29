package com.healthlink.Services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GeminiService {
    // URL de l'API Gemini mise à jour avec le nom de modèle correct
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final String apiKey;
    private final HttpClient client;

    public GeminiService(String apiKey) {
        this.apiKey = apiKey;
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<String> generateForumContent(String topic) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Construire l'URL avec la clé API
                String fullUrl = API_URL + "?key=" + apiKey;

                // Créer le corps de la requête selon le format attendu par Gemini
                JSONObject requestBody = new JSONObject();

                // Définir le contenu de la demande
                JSONObject contents = new JSONObject();
                contents.put("role", "user");

                JSONObject textPart = new JSONObject();
                textPart.put("text", "Génère un contenu pour un forum médical sur le sujet: '" + topic + "'. " +
                        "Format souhaité: Un titre engageant suivi d'une introduction informative, " +
                        "puis le contenu principal avec des informations pertinentes et une conclusion. " +
                        "Le tout doit être structuré, professionnel et informatif pour un contexte médical. " +
                        "Longueur: environ 100-300 mots maximum.");

                JSONArray parts = new JSONArray();
                parts.put(textPart);
                contents.put("parts", parts);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(contents);
                requestBody.put("contents", contentsArray);

                // Ajouter les paramètres de génération
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 800);
                requestBody.put("generationConfig", generationConfig);

                // Créer la requête HTTP
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(fullUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                // Envoyer la requête et traiter la réponse
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    JSONObject responseJson = new JSONObject(response.body());

                    // Extraire le contenu généré
                    String generatedContent = responseJson
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    return generatedContent.trim();
                } else {
                    System.err.println("Erreur API: " + response.statusCode() + " - " + response.body());
                    throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de la génération du contenu: " + e.getMessage(), e);
            }
        });
    }
}