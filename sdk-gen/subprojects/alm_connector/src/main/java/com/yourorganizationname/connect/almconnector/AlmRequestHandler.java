package com.yourorganizationname.connect.almconnector;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

public class AlmRequestHandler {
    private static final String PROP_NAME_CLIENT_ID = "client_id";
    private static final String PROP_NAME_CLIENT_SECRET = "client_secret";

    final String REFRESH_TOKEN_PATH = "/oauth/token";
    final String ACCESS_TOKEN_PATH = "/oauth/token/refresh";
    
    private String baseUrl;
    private String basicPostBody;

    private final HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER) // <--- WAŻNE
        .build();;


    public AlmRequestHandler(Properties connectionProps) {
        this.baseUrl = connectionProps.getProperty("host");
        this.basicPostBody = String.join("&", 
            String.join("=", PROP_NAME_CLIENT_ID, connectionProps.getProperty(PROP_NAME_CLIENT_ID)),
            String.join("=", PROP_NAME_CLIENT_SECRET, connectionProps.getProperty(PROP_NAME_CLIENT_SECRET))
        );
    }

    public JsonObject fetchRefreshToken(String codeParamValue) throws IOException, InterruptedException {
        return sendTokenRequest(REFRESH_TOKEN_PATH, "code", codeParamValue, "REFRESH_TOKEN");
    }

    public JsonObject fetchAccessToken(String refreshToken) throws IOException, InterruptedException {
        return sendTokenRequest(ACCESS_TOKEN_PATH, "refresh_token", refreshToken, "ACCESS_TOKEN");
    }

    public JsonObject sendAuthorizedGET(String path, String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(String.join("", this.baseUrl, path, "?access_token=", accessToken)))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
        return performRequestWithResponseCheck(request, "DATA_REQUEST");
    }

    private JsonObject sendTokenRequest(String path, String paramName, String paramValue, String stage) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.baseUrl + path))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(this.basicPostBody + "&" + paramName + "=" + paramValue))
            .build();
        return performRequestWithResponseCheck(request, stage);
    }

    private JsonObject performRequestWithResponseCheck(HttpRequest request, String stage) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed at stage " + stage + ". HTTP " + response.statusCode() + ": " + response.body());
        }
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
