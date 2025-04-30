package com.healthlink.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DeepLTranslationService {
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeepLTranslationService(String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /**
     * Translate text using DeepL API
     * @param text The text to translate
     * @param targetLanguage The target language code (e.g., "EN", "FR")
     * @return The translated text
     * @throws IOException If the translation fails
     */
    public String translate(String text, String targetLanguage) throws IOException {
        if (text == null || text.isEmpty()) {
            return text;
        }

        try {
            // Prepare the request body
            String requestBody = String.format("text=%s&target_lang=%s",
                    java.net.URLEncoder.encode(text, "UTF-8"),
                    targetLanguage);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-free.deepl.com/v2/translate"))
                    .header("Authorization", "DeepL-Auth-Key " + apiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Parse the response
            Map<String, Object> responseData = objectMapper.readValue(
                    response.body(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            );

            // Extract the translation
            List<Map<String, String>> translations = (List<Map<String, String>>) responseData.get("translations");
            if (translations != null && !translations.isEmpty()) {
                return translations.get(0).get("text");
            }

            return text; // Return original text if no translation found

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Translation interrupted", e);
        } catch (Exception e) {
            throw new IOException("Translation failed: " + e.getMessage(), e);
        }
    }
}