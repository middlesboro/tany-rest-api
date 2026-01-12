package sk.tany.rest.api.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;
import sk.tany.rest.api.service.client.ReviewClientService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Client Controller")
public class ReviewClientController {

    private final ReviewClientService service;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all reviews by product id")
    public Page<ReviewClientListResponse> findAllByProductId(@PathVariable String productId, Pageable pageable) {
        return service.findAllByProductId(productId, pageable);
    }
}
