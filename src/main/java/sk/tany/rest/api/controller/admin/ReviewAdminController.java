package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.review.ReviewAdminCreateRequest;
import sk.tany.rest.api.dto.admin.review.ReviewAdminDetailResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminListResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminUpdateRequest;
import sk.tany.rest.api.service.admin.ReviewAdminService;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Admin Controller")
@PreAuthorize("hasRole('ADMIN')")
public class ReviewAdminController {

    private final ReviewAdminService service;

    @GetMapping
    @Operation(summary = "Get all reviews")
    public Page<ReviewAdminListResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by id")
    public ReviewAdminDetailResponse findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create review")
    public ReviewAdminDetailResponse create(@RequestBody ReviewAdminCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update review")
    public ReviewAdminDetailResponse update(@PathVariable String id, @RequestBody ReviewAdminUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PostMapping("/import")
    @Operation(summary = "Import reviews from resources/reviews.json")
    public void importReviews() {
        service.importReviews();
    }
}
