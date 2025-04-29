package com.healthlink.Services;

import com.google.gson.*;
import java.net.http.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FactCheckService {
    private static final String API_KEY = "AIzaSyDY-j8GuLpCDW5Q4aRdoU2THcOlx1hIwTk";
    private static final String API_URL = "https://factchecktools.googleapis.com/v1alpha1/claims:search";

    public List<Map<String, String>> verifyMedicalClaim(String claim) throws Exception {
        // Debug 1 : Vérifiez que la méthode est appelée
        System.out.println("Tentative de vérification : " + claim);

        String encodedQuery = URLEncoder.encode(claim, StandardCharsets.UTF_8);
        String fullUrl = API_URL + "?query=" + encodedQuery + "&key=" + API_KEY;

        // Debug 2 : Affichez l'URL complète
        System.out.println("Requête API : " + fullUrl);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Debug 3 : Affichez la réponse brute
        System.out.println("Réponse API : " + response.body());

        return parseApiResponse(response.body());
    }

    private List<Map<String, String>> parseApiResponse(String json) {
        List<Map<String, String>> results = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        if (root.has("claims")) {
            JsonArray claims = root.getAsJsonArray("claims");
            for (JsonElement claim : claims) {
                Map<String, String> result = new HashMap<>();
                JsonObject claimObj = claim.getAsJsonObject();

                result.put("text", claimObj.get("text").getAsString());
                result.put("claimant", claimObj.has("claimant") ? claimObj.get("claimant").getAsString() : "Inconnu");

                if (claimObj.has("claimReview")) {
                    JsonArray reviews = claimObj.getAsJsonArray("claimReview");
                    if (reviews.size() > 0) {
                        JsonObject review = reviews.get(0).getAsJsonObject();
                        result.put("rating", review.get("textualRating").getAsString());
                        result.put("publisher", review.get("publisher").getAsJsonObject().get("name").getAsString());
                        result.put("url", review.get("url").getAsString());
                    }
                }

                results.add(result);
            }
        }
        return results;
    }

}