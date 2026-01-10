package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryAdminService {
    Page<CategoryDto> findAll(Pageable pageable);
    Optional<CategoryDto> findById(String id);
    List<String> findIdsByPrestashopIds(List<Long> prestashopIds);
    CategoryDto save(CategoryDto categoryDto);
    CategoryDto update(String id, CategoryDto categoryDto);
    void deleteById(String id);
}
