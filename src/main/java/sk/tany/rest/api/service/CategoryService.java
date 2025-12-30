package sk.tany.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CategoryDto;

import java.util.Optional;

public interface CategoryService {
    Page<CategoryDto> findAll(Pageable pageable);
    Optional<CategoryDto> findById(String id);
    CategoryDto save(CategoryDto categoryDto);
    CategoryDto update(String id, CategoryDto categoryDto);
    void deleteById(String id);
}
