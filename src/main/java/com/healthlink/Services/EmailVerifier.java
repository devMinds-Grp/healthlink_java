package com.healthlink.Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class EmailVerifier {

    // Remplacez par votre clé API QuickEmailVerification
    private static final String API_KEY = "c464aa261786ce2e30fd8470484362fc4c0d2cee0dd7c9aa6f08c7944083";

    public static boolean verifierEmail(String email) {
        try {
            // URL de l'API QuickEmailVerification
            String urlString = "https://api.quickemailverification.com/v1/verify?email=" + email + "&apikey=" + API_KEY;
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // Lire la réponse
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Réponse API : " + response.toString());

            // Parser la réponse JSON
            JSONObject json = new JSONObject(response.toString());

            // Vérifier si la requête a échoué
            if (!json.getString("result").equals("valid")) {
                System.out.println("Email invalide : " + json.getString("reason"));
                return false;
            }

            // Vérifications supplémentaires
            boolean disposable = json.getBoolean("disposable");
            boolean acceptAll = json.getBoolean("accept_all");
            boolean free = json.getBoolean("free");

            System.out.println("disposable = " + disposable + ", accept_all = " + acceptAll + ", free = " + free);

            // Accepter l'email si :
            // - Il est valide (result = "valid")
            // - Il n'est pas temporaire (disposable = false)
            // - Il n'est pas un domaine "catch-all" (accept_all = false, si souhaité)
            return !disposable && !acceptAll;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
