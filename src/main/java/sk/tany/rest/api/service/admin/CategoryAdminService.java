package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CategoryDto;

import java.util.Optional;

public interface CategoryAdminService {
    Page<CategoryDto> findAll(Pageable pageable);
    Page<CategoryDto> findAll(String query, Pageable pageable);
    Optional<CategoryDto> findById(String id);
    CategoryDto save(CategoryDto categoryDto);
    CategoryDto update(String id, CategoryDto categoryDto);
    CategoryDto patch(String id, sk.tany.rest.api.dto.admin.category.patch.CategoryPatchRequest patchDto);
    void deleteById(String id);
    Optional<CategoryDto> findByPrestashopId(Long prestashopId);
}
