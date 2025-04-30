package com.healthlink.Controllers.User.Authentification;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class RecaptchaHandler {
//    private static final String SITE_KEY = "6LfsZiYrAAAAADTjX-5XWFV-93UooXm2iTqJ9AIu"; // ta clé site correcte
//    private static final String SECRET_KEY = "6LfsZiYrAAAAAF_SegLm5ZjC8YKNMTKK7TL5WE1W"; // ta clé secrète correcte
//    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
//
//    private String lastToken;
//
//    public void initRecaptcha(WebView webView) {
//        WebEngine engine = webView.getEngine();
//        engine.setJavaScriptEnabled(true);
//
//        String htmlContent = """
//        <!DOCTYPE html>
//        <html>
//        <head>
//            <script src="https://www.google.com/recaptcha/api.js" async defer></script>
//        </head>
//        <body>
//            <form id="recaptcha-form">
//                <div class="g-recaptcha" data-sitekey="6LfsZiYrAAAAADTjX-5XWFV-93UooXm2iTqJ9AIu"></div>
//                <br/>
//                <input type="submit" value="Valider">
//            </form>
//
//            <script>
//                document.getElementById('recaptcha-form').addEventListener('submit', function(e) {
//                    e.preventDefault();
//                    var response = grecaptcha.getResponse();
//                    if(response.length == 0) {
//                        alert("Veuillez cocher la case pour prouver que vous n'êtes pas un robot.");
//                    } else {
//                        alert(response);
//                    }
//                });
//            </script>
//        </body>
//        </html>
//        """;
//
//        engine.loadContent(htmlContent);
//
//        engine.setOnAlert(event -> {
//            lastToken = event.getData();
//            System.out.println("Token reCAPTCHA reçu : " + lastToken);
//        });
//
//        engine.setOnError(event -> {
//            System.out.println("Erreur WebView : " + event.getMessage());
//        });
//    }
//
//
//    public String getLastToken() {
//        return lastToken;
//    }
//
//    public static boolean isTokenValid(String token) throws IOException {
//        URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
//        String params = "secret=" + SECRET_KEY + "&response=" + token;
//
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setDoOutput(true);
//
//        try (OutputStream outputStream = connection.getOutputStream()) {
//            outputStream.write(params.getBytes());
//            outputStream.flush();
//        }
//
//        StringBuilder response = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//        }
//
//        JsonObject responseJson = JsonParser.parseString(response.toString()).getAsJsonObject();
//
//        boolean success = responseJson.get("success").getAsBoolean();
//        if (!success) {
//            System.out.println("Erreur reCAPTCHA : " + responseJson);
//            return false;
//        }
//
//        // Maintenant que success == true, tu peux lire le score
//        double score = responseJson.get("score").getAsDouble();
//        System.out.println("Score reCAPTCHA : " + score);
//
//        return score >= 0.5;
//    }

}
