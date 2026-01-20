package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.mapper.CategoryMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryAdminServiceImpl implements CategoryAdminService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductSearchEngine productSearchEngine;

    @Override
    public Page<CategoryDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toDto);
    }

    @Override
    public Page<CategoryDto> findAll(String query, Pageable pageable) {
        return productSearchEngine.searchCategories(query, pageable).map(categoryMapper::toDto);
    }

    @Override
    public Optional<CategoryDto> findById(String id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        var category = categoryMapper.toEntity(categoryDto);
        var savedCategory = categoryRepository.save(category);
        productSearchEngine.addCategory(savedCategory);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto update(String id, CategoryDto categoryDto) {
        categoryDto.setId(id);
        var category = categoryMapper.toEntity(categoryDto);
        var savedCategory = categoryRepository.save(category);
        productSearchEngine.updateCategory(savedCategory);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto patch(String id, sk.tany.rest.api.dto.admin.category.patch.CategoryPatchRequest patchDto) {
        var category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        categoryMapper.updateEntityFromPatch(patchDto, category);
        var savedCategory = categoryRepository.save(category);
        productSearchEngine.updateCategory(savedCategory);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public void deleteById(String id) {
        categoryRepository.deleteById(id);
        productSearchEngine.removeCategory(id);
    }

    @Override
    public Optional<CategoryDto> findByPrestashopId(Long prestashopId) {
        return categoryRepository.findByPrestashopId(prestashopId).map(categoryMapper::toDto);
    }
}
