package sk.tany.rest.api.service.impl;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class OneDriveTokenCredential implements TokenCredential {

    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private String refreshToken;
    private final java.util.function.Consumer<String> onTokenUpdate;
    private AccessToken currentAccessToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OneDriveTokenCredential(String clientId, String clientSecret, String tenantId, String refreshToken, java.util.function.Consumer<String> onTokenUpdate) {
        this(clientId, clientSecret, tenantId, refreshToken, onTokenUpdate, HttpClient.newHttpClient());
    }

    public OneDriveTokenCredential(String clientId, String clientSecret, String tenantId, String refreshToken, java.util.function.Consumer<String> onTokenUpdate, HttpClient httpClient) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId != null ? tenantId : "consumers";
        this.refreshToken = refreshToken;
        this.onTokenUpdate = onTokenUpdate;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.fromCallable(() -> {
            if (currentAccessToken != null && !currentAccessToken.isExpired()) {
                return currentAccessToken;
            }
            return acquireAccessToken();
        });
    }

    private synchronized AccessToken acquireAccessToken() {
        // Double check locking
        if (currentAccessToken != null && !currentAccessToken.isExpired()) {
            return currentAccessToken;
        }

        try {
            log.debug("Acquiring OneDrive access token...");
            String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

            Map<String, String> parameters = new HashMap<>();
            parameters.put("client_id", clientId);
            if (clientSecret != null && !clientSecret.isEmpty()) {
                parameters.put("client_secret", clientSecret);
            }

            parameters.put("refresh_token", refreshToken);
            parameters.put("grant_type", "refresh_token");

            String form = parameters.entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Failed to acquire token. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Failed to acquire token: " + response.body());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            String accessToken = rootNode.get("access_token").asText();
            int expiresIn = rootNode.get("expires_in").asInt();

            // Update refresh token if provided
            if (rootNode.has("refresh_token")) {
                String newRefreshToken = rootNode.get("refresh_token").asText();
                this.refreshToken = newRefreshToken;
                if (onTokenUpdate != null) {
                    onTokenUpdate.accept(newRefreshToken);
                }
            }

            // Expiration buffer of 5 minutes
            OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(expiresIn - 300);
            this.currentAccessToken = new AccessToken(accessToken, expiresAt);

            log.debug("OneDrive access token acquired successfully.");
            return currentAccessToken;

        } catch (Exception e) {
            log.error("Error acquiring token", e);
            throw new RuntimeException("Error acquiring token", e);
        }
    }
}
