package sk.tany.rest.api.service.impl;

import com.azure.core.credential.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshTokenCredentialTest {

    @Test
    void testGetToken() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String jsonResponse = "{\"access_token\":\"new_access_token\",\"expires_in\":3600,\"refresh_token\":\"new_refresh_token\"}";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);

        RefreshTokenCredential credential = new RefreshTokenCredential("client", "secret", "consumers", "refresh", mockHttpClient);

        AccessToken token = credential.getToken(null).block();

        Assertions.assertNotNull(token);
        Assertions.assertEquals("new_access_token", token.getToken());

        // Verify expiration is set (roughly now + 3600 - 300)
        Assertions.assertNotNull(token.getExpiresAt());
    }
}
