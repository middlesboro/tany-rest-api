package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.admin.category.patch.CategoryPatchRequest;
import sk.tany.rest.api.service.admin.CategoryAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryAdminService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto category) {
        CategoryDto savedCategory = categoryService.save(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<CategoryDto> getCategories(Pageable pageable) {
        return categoryService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable String id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable String id, @RequestBody CategoryDto category) {
        CategoryDto updatedCategory = categoryService.update(id, category);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryDto> patchCategory(@PathVariable String id, @RequestBody CategoryPatchRequest patchDto) {
        CategoryDto updatedCategory = categoryService.patch(id, patchDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
