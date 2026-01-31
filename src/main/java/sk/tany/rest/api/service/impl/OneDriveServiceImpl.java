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

    private GraphServiceClient graphClient;

    @PostConstruct
    public void init() {
        if (clientId == null || clientId.isEmpty()) {
            log.warn("OneDrive client-id is missing. OneDrive integration will be disabled.");
            return;
        }
        try {
            OneDriveTokenCredential credential = new OneDriveTokenCredential(clientId, clientSecret, tenantId, null);
            graphClient = new GraphServiceClient(credential, "https://graph.microsoft.com/.default");
            log.info("Initialized OneDrive service with Custom Credential (ClientCredentials/RefreshToken flow)");
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

            Drive drive = graphClient.users().byUserId(userPrincipalName).drive().get();
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
