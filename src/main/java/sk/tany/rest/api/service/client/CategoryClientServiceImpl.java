package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.mapper.CategoryMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryClientServiceImpl implements CategoryClientService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDto).toList();
    }

    @Override
    public Optional<CategoryDto> findById(String id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }
}
