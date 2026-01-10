package sk.tany.rest.api.service.common;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.exceptions.InternalServerException;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.GetFileListRequest;
import io.imagekit.sdk.models.results.Result;
import io.imagekit.sdk.models.results.ResultList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageKitService implements ImageService {

    private final ImageKit imageKit;

    @Override
    public String upload(MultipartFile file, ImageKitType type) {
        try {
            return upload(file.getBytes(), UUID.randomUUID().toString(), type);
        } catch (IOException e) {
            log.error("Error while reading file bytes", e);
            throw new RuntimeException("Error while reading file bytes", e);
        }
    }

    @Override
    public String upload(byte[] file, String fileName, ImageKitType type) {
        try {
            FileCreateRequest fileCreateRequest = new FileCreateRequest(file, fileName);
            if (type != null) {
                switch (type) {
                    case PRODUCT -> fileCreateRequest.setFolder("products");
                    case BRAND -> fileCreateRequest.setFolder("brands");
                    case CARRIER -> fileCreateRequest.setFolder("carriers");
                    case PAYMENT_METHOD -> fileCreateRequest.setFolder("payment_methods");
                }
            }
            Result result = imageKit.upload(fileCreateRequest);
            return result.getUrl();
        } catch (InternalServerException e) {
            log.error("Error while uploading image to ImageKit", e);
            throw new RuntimeException("Error while uploading image to ImageKit", e);
        } catch (Exception e) {
            log.error("Unexpected error while uploading image to ImageKit", e);
            throw new RuntimeException("Unexpected error while uploading image to ImageKit", e);
        }
    }

    @Override
    public void delete(String url) {
        try {
            String fileName = getFileNameFromUrl(url);
            GetFileListRequest getFileListRequest = new GetFileListRequest();
            getFileListRequest.setSearchQuery("name=\"" + fileName + "\"");
            ResultList resultList = imageKit.getFileList(getFileListRequest);
            if (resultList.getResults().isEmpty()) {
                log.warn("Image with url {} not found in ImageKit", url);
                return;
            }
            // Assuming the first file is the one we want to delete
            // Since we use UUIDs for filenames, it should be unique
            String fileId = resultList.getResults().get(0).getFileId();
            imageKit.deleteFile(fileId);
            log.info("Image {} deleted successfully", url);
        } catch (Exception e) {
            log.error("Error while deleting image from ImageKit", e);
            throw new RuntimeException("Error while deleting image from ImageKit", e);
        }
    }

    private String getFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return url;
        }
        return url.substring(lastSlashIndex + 1);
    }
}
