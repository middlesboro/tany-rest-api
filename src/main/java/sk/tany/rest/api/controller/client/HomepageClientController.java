package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.client.homepage.HomepageGridResponse;
import sk.tany.rest.api.service.client.HomepageClientService;

@RestController
@RequestMapping("/api/homepage-grids")
@RequiredArgsConstructor
public class HomepageClientController {

    private final HomepageClientService homepageClientService;

    @GetMapping
    public ResponseEntity<HomepageGridResponse> getHomepageGrids() {
        return ResponseEntity.ok(homepageClientService.getHomepageGrids());
    }
}
