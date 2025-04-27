package com.healthlink.Services;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GoogleAuthService {
    private static final String CLIENT_ID = "820698692828-r8tk2houd2hs1of9uenm7oam4mklp1vt.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    private final GoogleAuthorizationCodeFlow flow;

    public GoogleAuthService() {
        flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                CLIENT_ID,
                CLIENT_SECRET,
                SCOPES)
                .build();
    }

    public String getAuthorizationUrl() {
        return flow.newAuthorizationUrl()
                .setRedirectUri("http://localhost:8888/Callback")
                .build();
    }

    public GoogleTokenResponse getTokenResponse(String code) throws IOException {
        return flow.newTokenRequest(code)
                .setRedirectUri("http://localhost:8888/Callback")
                .execute();
    }
}
