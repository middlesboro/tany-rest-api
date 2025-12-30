package sk.tany.rest.api.service;

import sk.tany.rest.api.dto.CategoryDto;

import java.util.Optional;

public interface CategoryService {
    Optional<CategoryDto> findById(String id);
    CategoryDto save(CategoryDto categoryDto);
    CategoryDto update(String id, CategoryDto categoryDto);
    void deleteById(String id);
}
