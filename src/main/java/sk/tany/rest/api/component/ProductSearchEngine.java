package sk.tany.rest.api.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productlabel.ProductLabelRepository;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.mapper.ProductLabelMapper;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchEngine {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductLabelRepository productLabelRepository;
    private final ProductSalesRepository productSalesRepository;
    private final ProductLabelMapper productLabelMapper;

    public List<ProductLabelDto> getProductLabels(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .map(id -> productLabelRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .filter(pl -> pl.isActive())
                .map(productLabelMapper::toDto)
                .toList();
    }

    public Integer getSalesCount(String productId) {
        return productSalesRepository.findByProductId(productId)
                .map(ps -> ps.getSalesCount())
                .orElse(0);
    }

    public Page<Category> searchCategories(String query, Pageable pageable) {
        return categoryRepository.searchCategories(query, pageable);
    }

    public List<Product> searchAndSort(String query) {
        return productRepository.searchAndSort(query);
    }

    public List<FilterParameterDto> getFilterParametersForCategory(String categoryId) {
        // We can reuse the method with null filter, as the logic handles it
        return getFilterParametersForCategoryWithFilter(categoryId, null);
    }

    public List<Product> search(String categoryId, CategoryFilterRequest request) {
        return productRepository.search(categoryId, request);
    }

    public Page<Product> search(ProductFilter filter, Pageable pageable) {
        return productRepository.search(filter, pageable);
    }

    public List<FilterParameterDto> getFilterParametersForCategoryWithFilter(String categoryId, CategoryFilterRequest filterRequest) {
        return productRepository.getFilterParametersForCategoryWithFilter(categoryId, filterRequest);
    }
}
