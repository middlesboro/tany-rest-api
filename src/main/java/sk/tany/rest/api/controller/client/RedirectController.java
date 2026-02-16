package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.CategoryRepository;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @GetMapping("/content/{id:\\d+}-{slug}")
    public ResponseEntity<Void> redirectContent(@PathVariable String id, @PathVariable String slug) {
        URI location = UriComponentsBuilder.fromPath("/{slug}")
                .buildAndExpand(slug)
                .toUri();
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(location)
                .build();
    }

    @GetMapping("/blog/{slug}-b{id:\\d+}.html")
    public ResponseEntity<Void> redirectBlog(@PathVariable String slug, @PathVariable String id) {
        URI location = UriComponentsBuilder.fromPath("/blog/{slug}")
                .buildAndExpand(slug)
                .toUri();
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(location)
                .build();
    }

    @GetMapping("/{id:\\d+}-{slug}")
    public ResponseEntity<Void> redirectCategoryOrBrand(@PathVariable String id, @PathVariable String slug) {
        // 1. Try to find category by slug
        if (categoryRepository.findBySlug(slug).isPresent()) {
            URI location = UriComponentsBuilder.fromPath("/kategoria/{slug}")
                    .buildAndExpand(slug)
                    .toUri();
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(location)
                    .build();
        }

        long prestashopId;
        try {
            prestashopId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.notFound().build();
        }

        // 2. Try to find category by prestashopId
        var category = categoryRepository.findByPrestashopId(prestashopId);
        if (category.isPresent()) {
            URI location = UriComponentsBuilder.fromPath("/kategoria/{slug}")
                    .buildAndExpand(category.get().getSlug())
                    .toUri();
             return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(location)
                    .build();
        }

        // 3. Try to find brand by prestashopId
        var brand = brandRepository.findByPrestashopId(prestashopId);
        if (brand.isPresent()) {
            URI location = UriComponentsBuilder.fromPath("/kategoria/vsetky-produkty")
                    .queryParam("q", "Brand-" + brand.get().getName())
                    .build()
                    .toUri();
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(location)
                    .build();
        }

        return ResponseEntity.notFound().build();
    }
}
