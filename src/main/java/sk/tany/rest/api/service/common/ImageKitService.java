package sk.tany.rest.api.service.common;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.exceptions.InternalServerException;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
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
                    case SUPPLIER -> fileCreateRequest.setFolder("suppliers");
                    case BRAND -> fileCreateRequest.setFolder("brands");
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
}
