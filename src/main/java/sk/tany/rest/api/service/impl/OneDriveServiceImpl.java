package sk.tany.rest.api.service.impl;

import com.microsoft.graph.models.Drive;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.service.OneDriveService;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneDriveServiceImpl implements OneDriveService {

    @Value("${onedrive.client-id:}")
    private String clientId;

    @Value("${onedrive.client-secret:}")
    private String clientSecret;

    @Value("${onedrive.tenant-id:}")
    private String tenantId;

    @Value("${onedrive.user-principal-name:}")
    private String userPrincipalName;

    @Value("${onedrive.refresh-token:}")
    private String refreshToken;

    private GraphServiceClient graphClient;
    private static final String TOKEN_FILE_PATH = "onedrive_token.dat";

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

            OneDriveTokenCredential credential = new OneDriveTokenCredential(clientId, clientSecret, tenantId, resolvedToken, this::persistToken);
            graphClient = new GraphServiceClient(credential, "https://graph.microsoft.com/.default");
            log.info("Initialized OneDrive service with Personal Account (RefreshToken flow)");
        } catch (Exception e) {
            log.error("Failed to initialize OneDrive client", e);
        }
    }

    private String loadPersistedToken() {
        try {
            java.io.File file = new java.io.File(TOKEN_FILE_PATH);
            if (file.exists()) {
                return java.nio.file.Files.readString(file.toPath()).trim();
            }
        } catch (Exception e) {
            log.warn("Failed to load persisted OneDrive token", e);
        }
        return null;
    }

    private void persistToken(String token) {
        try {
            java.nio.file.Files.writeString(new java.io.File(TOKEN_FILE_PATH).toPath(), token);
            log.info("OneDrive refresh token persisted securely.");
        } catch (Exception e) {
            log.error("Failed to persist OneDrive token", e);
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
