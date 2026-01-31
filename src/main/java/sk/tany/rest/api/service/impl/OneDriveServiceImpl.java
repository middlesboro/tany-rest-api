package sk.tany.rest.api.service.impl;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
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

    @PostConstruct
    public void init() {
        if (clientId == null || clientId.isEmpty()) {
            log.warn("OneDrive client-id is missing. OneDrive integration will be disabled.");
            return;
        }
        try {
            if (refreshToken != null && !refreshToken.isEmpty()) {
                // Personal account configuration
                RefreshTokenCredential credential = new RefreshTokenCredential(clientId, clientSecret, tenantId, refreshToken);
                graphClient = new GraphServiceClient(credential, "https://graph.microsoft.com/.default");
                log.info("Initialized OneDrive service with Personal Account (RefreshToken flow)");
            } else {
                // Business account configuration
                ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .tenantId(tenantId)
                        .build();

                graphClient = new GraphServiceClient(credential, "https://graph.microsoft.com/.default");
                log.info("Initialized OneDrive service with Business Account (ClientCredentials flow)");
            }
        } catch (Exception e) {
            log.error("Failed to initialize OneDrive client", e);
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

            Drive drive;
            if (refreshToken != null && !refreshToken.isEmpty()) {
                drive = graphClient.me().drive().get();
            } else {
                drive = graphClient.users().byUserId(userPrincipalName).drive().get();
            }
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
