package sk.tany.rest.api.service.common;

import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.service.common.enums.ImageKitType;

public interface ImageService {
    String upload(MultipartFile file, ImageKitType type);
    String upload(byte[] file, String fileName, ImageKitType type);
    void delete(String url);
}
