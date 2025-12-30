package sk.tany.rest.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String upload(MultipartFile file);
}
