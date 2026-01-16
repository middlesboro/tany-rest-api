package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.mapper.CategoryMapper;

import sk.tany.rest.api.domain.product.Product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryClientServiceImpl implements CategoryClientService {

    private static final List<Long> EXCLUDED_PRESTASHOP_IDS = List.of(1L, 2L);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductSearchEngine productSearchEngine;

    @Override
    public List<CategoryDto> findAllVisible() {
        List<CategoryDto> allCategories = categoryRepository.findAll().stream()
                .filter(c -> !EXCLUDED_PRESTASHOP_IDS.contains(c.getPrestashopId()) && c.isActive() && c.isVisible())
                .sorted(Comparator.comparingLong(Category::getPosition))
                .map(categoryMapper::toDto)
                .toList();

        for (CategoryDto category : allCategories) {
            category.setFilterParameters(productSearchEngine.getFilterParametersForCategory(category.getId()));
        }

        Map<String, List<CategoryDto>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryDto::getParentId));

        for (CategoryDto category : allCategories) {
            category.setChildren(childrenMap.getOrDefault(category.getId(), new ArrayList<>())
                    .stream()
                    .sorted(Comparator.comparingLong(value -> category.getPosition())).toList()
            );
        }

        return allCategories.stream()
                .filter(c -> c.getPrestashopParentId() == 2L)
                .toList();
    }

    @Override
    public Optional<CategoryDto> findById(String id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }

    @Override
    public List<FilterParameterDto> getFilterParameters(String categoryId, CategoryFilterRequest request) {
        return productSearchEngine.getFilterParametersForCategoryWithFilter(categoryId, request);
    }

    @Override
    public sk.tany.rest.api.dto.response.CategoryClientResponse getCategoryData(CategoryFilterRequest request) {
        List<Product> filteredProducts = productSearchEngine.filterProducts(request);

        Set<String> relevantCategoryIds = filteredProducts.stream()
                .filter(p -> p.getCategoryIds() != null)
                .flatMap(p -> p.getCategoryIds().stream())
                .collect(Collectors.toSet());

        List<CategoryDto> allCategories = categoryRepository.findAll().stream()
                .filter(c -> !EXCLUDED_PRESTASHOP_IDS.contains(c.getPrestashopId()) && c.isActive() && c.isVisible())
                .sorted(Comparator.comparingLong(Category::getPosition))
                .map(categoryMapper::toDto)
                .toList();

        Set<String> finalCategoryIdsToKeep = new HashSet<>();
        if (relevantCategoryIds.isEmpty()) {
             if (request == null || request.getFilterParameters() == null || request.getFilterParameters().isEmpty()) {
                  finalCategoryIdsToKeep.addAll(allCategories.stream().map(CategoryDto::getId).toList());
             }
        } else {
             Map<String, CategoryDto> categoryMap = allCategories.stream()
                     .collect(Collectors.toMap(CategoryDto::getId, Function.identity()));

             for (String catId : relevantCategoryIds) {
                 CategoryDto current = categoryMap.get(catId);
                 while (current != null) {
                     finalCategoryIdsToKeep.add(current.getId());
                     if (current.getParentId() == null) break;
                     current = categoryMap.get(current.getParentId());
                 }
             }
        }

        List<CategoryDto> filteredCategories = allCategories.stream()
                .filter(c -> finalCategoryIdsToKeep.contains(c.getId()))
                .toList();

        Map<String, List<CategoryDto>> childrenMap = filteredCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryDto::getParentId));

        for (CategoryDto category : filteredCategories) {
            category.setChildren(childrenMap.getOrDefault(category.getId(), new ArrayList<>())
                    .stream()
                    .sorted(Comparator.comparingLong(value -> category.getPosition())).toList()
            );
        }

        List<CategoryDto> rootCategories = filteredCategories.stream()
                .filter(c -> c.getPrestashopParentId() == 2L)
                .toList();

        List<Product> baseProducts;
        if (filteredCategories.isEmpty()) {
            baseProducts = List.of();
        } else {
            List<Product> allProducts = productSearchEngine.filterProducts(null);
            baseProducts = allProducts.stream()
                    .filter(p -> p.getCategoryIds() != null && p.getCategoryIds().stream().anyMatch(finalCategoryIdsToKeep::contains))
                    .toList();
        }

        List<sk.tany.rest.api.dto.ClientFilterParameterDto> filters = productSearchEngine.getClientFilterParameters(baseProducts, filteredProducts, request);

        sk.tany.rest.api.dto.response.CategoryClientResponse response = new sk.tany.rest.api.dto.response.CategoryClientResponse();
        response.setCategories(rootCategories);
        response.setFilterParameters(filters);

        return response;
    }
}
