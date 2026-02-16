package sk.tany.rest.api.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.service.feed.HeurekaFeedService;

import java.io.File;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "Feeds", description = "Endpoints for retrieving XML feeds")
public class FeedController {

    private final HeurekaFeedService heurekaFeedService;

    @Operation(summary = "Get Heureka product feed")
    @GetMapping(value = "/heureka/products", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Resource> getHeurekaProductFeed() {
        File file = heurekaFeedService.getProductFeedFile();
        if (file == null || !file.exists()) {
            heurekaFeedService.generateProductFeed();
            file = heurekaFeedService.getProductFeedFile();
        }

        if (file == null || !file.exists()) {
             return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(new FileSystemResource(file));
    }

    @Operation(summary = "Get Heureka availability feed")
    @GetMapping(value = "/heureka/availability", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Resource> getHeurekaAvailabilityFeed() {
        File file = heurekaFeedService.getAvailabilityFeedFile();
        if (file == null || !file.exists()) {
            heurekaFeedService.generateAvailabilityFeed();
            file = heurekaFeedService.getAvailabilityFeedFile();
        }

        if (file == null || !file.exists()) {
             return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(new FileSystemResource(file));
    }
}
