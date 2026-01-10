package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.mapper.CategoryMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryClientServiceImpl implements CategoryClientService {

    private static final List<Long> EXCLUDED_PRESTASHOP_IDS = List.of(1L, 2L);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> findAll() {
        List<CategoryDto> allCategories = categoryRepository.findAll().stream()
                .filter(c -> !EXCLUDED_PRESTASHOP_IDS.contains(c.getPrestashopId()))
                .map(categoryMapper::toDto)
                .toList();

        Map<String, List<CategoryDto>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryDto::getParentId));

        for (CategoryDto category : allCategories) {
            category.setChildren(childrenMap.getOrDefault(category.getId(), new ArrayList<>()));
        }

        return allCategories.stream()
                .filter(c -> c.getPrestashopParentId() == 2L)
                .toList();
    }

    @Override
    public Optional<CategoryDto> findById(String id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }
}
