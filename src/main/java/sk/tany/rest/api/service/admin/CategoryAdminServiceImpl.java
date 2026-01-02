package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.mapper.CategoryMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryAdminServiceImpl implements CategoryAdminService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<CategoryDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toDto);
    }

    @Override
    public Optional<CategoryDto> findById(String id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        var category = categoryMapper.toEntity(categoryDto);
        var savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto update(String id, CategoryDto categoryDto) {
        categoryDto.setId(id);
        var category = categoryMapper.toEntity(categoryDto);
        var savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public void deleteById(String id) {
        categoryRepository.deleteById(id);
    }
}
