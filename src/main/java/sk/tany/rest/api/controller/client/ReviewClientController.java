package sk.tany.rest.api.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;
import sk.tany.rest.api.service.client.ReviewClientService;

import java.util.Collection;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Client Controller")
public class ReviewClientController {

    private final ReviewClientService service;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all reviews by product id")
    public ReviewClientProductResponse findAllByProductId(@PathVariable String productId, Pageable pageable) {
        return service.findAllByProductId(productId, pageable);
    }

    @GetMapping("/brand")
    @Operation(summary = "Get all reviews by brand ids")
    public ReviewClientProductResponse findAllByBrandIds(@RequestParam Collection<String> brandIds, Pageable pageable) {
        return service.findAllByBrandIds(brandIds, pageable);
    }

    @PostMapping
    @Operation(summary = "Create review")
    public void create(@RequestBody ReviewClientCreateRequest request) {
        service.create(request);
    }
}
