package sk.tany.rest.api.service.impl;

import com.microsoft.graph.models.Drive;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.onedrive.OneDriveToken;
import sk.tany.rest.api.domain.onedrive.OneDriveTokenRepository;
import sk.tany.rest.api.service.OneDriveService;

import java.io.ByteArrayInputStream;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneDriveServiceImpl implements OneDriveService {

    @Value("${onedrive.client-id:}")
    private String clientId;

    @Value("${onedrive.client-secret:}")
    private String clientSecret;

    @Value("${onedrive.refresh-token:}")
    private String refreshToken;

    private final OneDriveTokenRepository oneDriveTokenRepository;
    private GraphServiceClient graphClient;
    private static final String TOKEN_ID = "onedrive_token";

    @PostConstruct
    public void init() {
        if (clientId == null || clientId.isEmpty()) {
            log.warn("OneDrive client-id is missing. OneDrive integration will be disabled.");
            return;
        }
        try {
            String resolvedToken = loadPersistedToken();
            if (resolvedToken == null || resolvedToken.isEmpty()) {
                resolvedToken = refreshToken;
            }

            if (resolvedToken == null || resolvedToken.isEmpty()) {
                log.warn("OneDrive refresh token is missing. OneDrive integration will be disabled.");
                return;
            }

            OneDriveTokenCredential credential = new OneDriveTokenCredential(clientId, clientSecret, resolvedToken, this::persistToken);
            graphClient = new GraphServiceClient(credential, "https://graph.microsoft.com/.default");
            log.info("Initialized OneDrive service with Personal Account (RefreshToken flow)");
        } catch (Exception e) {
            log.error("Failed to initialize OneDrive client", e);
        }
    }

    private String loadPersistedToken() {
        return oneDriveTokenRepository.findById(TOKEN_ID)
                .map(OneDriveToken::getRefreshToken)
                .orElse(null);
    }

    private void persistToken(String token) {
        try {
            OneDriveToken entity = oneDriveTokenRepository.findById(TOKEN_ID).orElse(new OneDriveToken());
            entity.setId(TOKEN_ID);
            entity.setRefreshToken(token);
            entity.setUpdateDate(Instant.now());
            oneDriveTokenRepository.save(entity);
            log.info("OneDrive refresh token persisted securely to database.");
        } catch (Exception e) {
            log.error("Failed to persist OneDrive token to database", e);
        }
    }

    @Override
    public void uploadFile(String folderPath, String fileName, byte[] content) {
        if (graphClient == null) {
            log.warn("OneDrive client is not initialized. Skipping upload for {}", fileName);
            return;
        }

        try {
            String fullPath = folderPath.endsWith("/") ? folderPath + fileName : folderPath + "/" + fileName;

            Drive drive = graphClient.me().drive().get();
            String driveId = drive.getId();

            String relativePath = fullPath.startsWith("/") ? fullPath.substring(1) : fullPath;
            String itemPathId = "root:/" + relativePath + ":";

            graphClient.drives().byDriveId(driveId)
                    .items()
                    .byDriveItemId(itemPathId)
                    .content()
                    .put(new ByteArrayInputStream(content));

            log.info("Uploaded {} to OneDrive: {}", fileName, fullPath);
        } catch (Exception e) {
            log.error("Failed to upload file to OneDrive: " + fileName, e);
            throw new RuntimeException("Failed to upload to OneDrive", e);
        }
    }
}
