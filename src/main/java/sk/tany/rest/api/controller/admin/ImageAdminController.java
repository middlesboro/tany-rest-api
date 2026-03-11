package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/images")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ImageAdminController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") ImageKitType type) throws IOException {

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = UUID.randomUUID().toString();
        }

        String url = imageService.upload(file.getBytes(), originalFilename, type);
        return ResponseEntity.ok(url);
    }
}
