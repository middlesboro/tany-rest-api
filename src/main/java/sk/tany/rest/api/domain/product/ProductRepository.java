package sk.tany.rest.api.domain.product;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.dizitart.no2.Nitrite;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.FilterParameterRequest;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ProductRepository extends AbstractInMemoryRepository<Product> {

    // Dependencies needed for complex search logic
    private final FilterParameterRepository filterParameterRepository;
    private final FilterParameterValueRepository filterParameterValueRepository;
    private final ProductSalesRepository productSalesRepository;
    private final CategoryRepository categoryRepository;
    private final FilterParameterMapper filterParameterMapper;
    private final FilterParameterValueMapper filterParameterValueMapper;

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private static final int MAX_EDIT_DISTANCE = 2;

    // Cache for category hierarchy to avoid repeated DB lookups
    private final Map<String, List<String>> cachedCategoryChildren = new ConcurrentHashMap<>();

    // We use @Lazy to avoid circular dependencies if any exist, though ideally repo-to-repo deps should be minimal.
    // However, ProductRepository needs other repos for the complex search logic.
    public ProductRepository(Nitrite nitrite,
                             @Lazy FilterParameterRepository filterParameterRepository,
                             @Lazy FilterParameterValueRepository filterParameterValueRepository,
                             @Lazy ProductSalesRepository productSalesRepository,
                             @Lazy CategoryRepository categoryRepository,
                             FilterParameterMapper filterParameterMapper,
                             FilterParameterValueMapper filterParameterValueMapper) {
        super(nitrite, Product.class);
        this.filterParameterRepository = filterParameterRepository;
        this.filterParameterValueRepository = filterParameterValueRepository;
        this.productSalesRepository = productSalesRepository;
        this.categoryRepository = categoryRepository;
        this.filterParameterMapper = filterParameterMapper;
        this.filterParameterValueMapper = filterParameterValueMapper;
    }

    @Override
    public void init() {
        super.init();
        refreshCategoryCache();
    }

    // Call this when categories change
    public void refreshCategoryCache() {
        cachedCategoryChildren.clear();
        for (Category category : categoryRepository.findAll()) {
             if (category.getParentId() != null) {
                cachedCategoryChildren.computeIfAbsent(category.getParentId(), k -> new ArrayList<>()).add(category.getId());
            }
        }
    }

    public Optional<Product> findByPrestashopId(Long prestashopId) {
        return memoryCache.values().stream()
                .filter(p -> p.getPrestashopId() != null && p.getPrestashopId().equals(prestashopId))
                .findFirst();
    }

    public List<Product> findAllByProductFilterParametersFilterParameterValueId(String filterParameterValueId) {
        return memoryCache.values().stream()
                .filter(p -> p.getProductFilterParameters() != null && p.getProductFilterParameters().stream()
                        .anyMatch(param -> Objects.equals(param.getFilterParameterValueId(), filterParameterValueId)))
                .toList();
    }

    // --- Search Logic migrated from ProductSearchEngine ---

    public List<Product> searchAndSort(String query) {
        if (StringUtils.isBlank(query)) {
            return List.of();
        }

        String normalizedQuery = StringUtils.stripAccents(query.toLowerCase()).trim();
        String[] queryWords = normalizedQuery.split("\\s+");

        return memoryCache.values().stream()
                .filter(product -> {
                    if (product.getTitle() == null) {
                        return false;
                    }
                    String normalizedName = StringUtils.stripAccents(product.getTitle().toLowerCase());
                    String[] nameWords = normalizedName.split("\\s+");

                    return Arrays.stream(queryWords).allMatch(qWord ->
                            Arrays.stream(nameWords).anyMatch(nWord ->
                                    nWord.contains(qWord) || levenshtein.apply(nWord, qWord) <= MAX_EDIT_DISTANCE
                            )
                    );
                })
                .sorted((p1, p2) -> {
                    Double score1 = calculateRelevance(p1.getTitle(), normalizedQuery);
                    Double score2 = calculateRelevance(p2.getTitle(), normalizedQuery);
                    return score2.compareTo(score1);
                })
                .toList();
    }

    private Double calculateRelevance(String productName, String normalizedQuery) {
        if (productName == null) {
            return 0.0;
        }
        String normalizedName = StringUtils.stripAccents(productName.toLowerCase());
        return jaroWinkler.apply(normalizedName, normalizedQuery);
    }

    public Page<Product> search(ProductFilter filter, Pageable pageable) {
        if (filter == null) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        String normalizedQuery = null;
        String[] queryWords = null;

        if (StringUtils.isNotBlank(filter.query())) {
            normalizedQuery = StringUtils.stripAccents(filter.query().toLowerCase()).trim();
            queryWords = normalizedQuery.split("\\s+");
        }

        final String finalNormalizedQuery = normalizedQuery;
        final String[] finalQueryWords = queryWords;

        List<Product> filteredProducts = memoryCache.values().stream()
                .filter(p -> {
                    if (filter.id() != null && !filter.id().isEmpty() && !p.getId().equals(filter.id())) {
                        return false;
                    }
                    if (filter.brandId() != null && !filter.brandId().isEmpty() && !filter.brandId().equals(p.getBrandId())) {
                        return false;
                    }
                    if (filter.active() != null && p.isActive() != filter.active()) {
                        return false;
                    }
                    if (filter.quantity() != null && !filter.quantity().equals(p.getQuantity())) {
                        return false;
                    }
                    if (filter.externalStock() != null) {
                        if (filter.externalStock() && p.getStatus() != ProductStatus.AVAILABLE_ON_EXTERNAL_STOCK) {
                            return false;
                        }
                        if (!filter.externalStock() && p.getStatus() == ProductStatus.AVAILABLE_ON_EXTERNAL_STOCK) {
                            return false;
                        }
                    }
                    if (filter.priceFrom() != null && (p.getPrice() == null || p.getPrice().compareTo(filter.priceFrom()) < 0)) {
                        return false;
                    }
                    if (filter.priceTo() != null && (p.getPrice() == null || p.getPrice().compareTo(filter.priceTo()) > 0)) {
                        return false;
                    }

                    if (finalQueryWords != null) {
                        if (p.getTitle() == null) {
                            return false;
                        }
                        String normalizedName = StringUtils.stripAccents(p.getTitle().toLowerCase());
                        String[] nameWords = normalizedName.split("\\s+");

                        return Arrays.stream(finalQueryWords).allMatch(qWord ->
                                Arrays.stream(nameWords).anyMatch(nWord ->
                                        nWord.contains(qWord) || levenshtein.apply(nWord, qWord) <= MAX_EDIT_DISTANCE
                                )
                        );
                    }

                    return true;
                })
                .collect(Collectors.toCollection(ArrayList::new));

        if (pageable.getSort().isSorted()) {
            sort(filteredProducts, pageable.getSort());
        } else if (finalNormalizedQuery != null) {
            filteredProducts.sort((p1, p2) -> {
                Double score1 = calculateRelevance(p1.getTitle(), finalNormalizedQuery);
                Double score2 = calculateRelevance(p2.getTitle(), finalNormalizedQuery);
                return score2.compareTo(score1);
            });
        }

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filteredProducts, pageable, filteredProducts.size());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());

        if (start > filteredProducts.size()) {
            return new PageImpl<>(List.of(), pageable, filteredProducts.size());
        }

        List<Product> pageContent = filteredProducts.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredProducts.size());
    }

    public List<Product> search(String categoryId, CategoryFilterRequest request) {
        if (categoryId == null) {
            return List.of();
        }
        Set<String> categoryIds = getAllCategoryIdsIncludingSubcategories(categoryId);

        Comparator<Product> comparator;
        if (request != null && request.getSort() != null) {
            comparator = switch (request.getSort()) {
                case NAME_ASC -> Comparator.comparing(Product::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case NAME_DESC -> Comparator.comparing(Product::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed();
                case PRICE_ASC -> Comparator.comparing(Product::getPrice, Comparator.nullsLast(Comparator.naturalOrder()));
                case PRICE_DESC -> Comparator.comparing(Product::getPrice, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                case BEST_SELLING -> Comparator.comparing((Product p) -> getSalesCount(p.getId())).reversed();
            };
        } else {
            comparator = Comparator.comparing(Product::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        return memoryCache.values().stream()
                .filter(p -> p.getCategoryIds() != null && !Collections.disjoint(p.getCategoryIds(), categoryIds))
                .filter(p -> matchesFilter(p, request))
                .sorted(comparator)
                .toList();
    }

    public List<FilterParameterDto> getFilterParametersForCategoryWithFilter(String categoryId, CategoryFilterRequest filterRequest) {
        if (categoryId == null) {
            return List.of();
        }
        Set<String> categoryIds = getAllCategoryIdsIncludingSubcategories(categoryId);

        List<Product> productsInCategory = memoryCache.values().stream()
                .filter(p -> p.getCategoryIds() != null && !Collections.disjoint(p.getCategoryIds(), categoryIds))
                .toList();

        Set<String> selectedValueIds = new HashSet<>();
        if (filterRequest != null && filterRequest.getFilterParameters() != null) {
            for (FilterParameterRequest param : filterRequest.getFilterParameters()) {
                if (param.getFilterParameterValueIds() != null) {
                    selectedValueIds.addAll(param.getFilterParameterValueIds());
                }
            }
        }

        List<FilterParameterDto> allFacets = getFilterParametersForProducts(productsInCategory, selectedValueIds);

        // Calculate availability
        for (FilterParameterDto facet : allFacets) {
            CategoryFilterRequest otherFiltersRequest = createRequestExcludingFacet(filterRequest, facet.getId());

            // Filter products using all OTHER facets
            Set<String> availableValuesForFacet = productsInCategory.stream()
                    .filter(p -> matchesFilter(p, otherFiltersRequest))
                    .flatMap(p -> p.getProductFilterParameters() != null ? p.getProductFilterParameters().stream() : null)
                    .filter(Objects::nonNull)
                    .filter(pfp -> facet.getId().equals(pfp.getFilterParameterId()))
                    .map(ProductFilterParameter::getFilterParameterValueId)
                    .collect(Collectors.toSet());

            if (facet.getValues() != null) {
                for (FilterParameterValueDto valueDto : facet.getValues()) {
                    valueDto.setAvailable(availableValuesForFacet.contains(valueDto.getId()));
                }
            }
        }

        return allFacets;
    }

    // --- Helper Methods ---

    private Integer getSalesCount(String productId) {
        Optional<ProductSales> sales = productSalesRepository.findByProductId(productId);
        return sales.map(ProductSales::getSalesCount).orElse(0);
    }

    private Set<String> getAllCategoryIdsIncludingSubcategories(String categoryId) {
        // Need to ensure cache is populated if not already (or rely on init)
        if (cachedCategoryChildren.isEmpty()) {
            refreshCategoryCache();
        }
        Set<String> result = new HashSet<>();
        if (categoryId != null) {
            collectCategoryIds(categoryId, result);
        }
        return result;
    }

    private void collectCategoryIds(String categoryId, Set<String> result) {
        if (result.contains(categoryId)) return;
        result.add(categoryId);
        List<String> children = cachedCategoryChildren.get(categoryId);
        if (children != null) {
            for (String childId : children) {
                collectCategoryIds(childId, result);
            }
        }
    }

    private CategoryFilterRequest createRequestExcludingFacet(CategoryFilterRequest original, String facetIdToExclude) {
        if (original == null || original.getFilterParameters() == null) {
            return new CategoryFilterRequest();
        }
        CategoryFilterRequest newRequest = new CategoryFilterRequest();
        newRequest.setFilterParameters(original.getFilterParameters().stream()
                .filter(param -> !param.getId().equals(facetIdToExclude))
                .toList());
        return newRequest;
    }

    private boolean matchesFilter(Product product, CategoryFilterRequest filterRequest) {
        if (filterRequest == null || filterRequest.getFilterParameters() == null || filterRequest.getFilterParameters().isEmpty()) {
            return true;
        }

        if (product.getProductFilterParameters() == null) {
            return false;
        }

        for (FilterParameterRequest paramReq : filterRequest.getFilterParameters()) {
            if (paramReq.getFilterParameterValueIds() == null || paramReq.getFilterParameterValueIds().isEmpty()) {
                continue;
            }

            boolean matchFound = product.getProductFilterParameters().stream()
                    .anyMatch(pfp -> pfp.getFilterParameterId() != null &&
                            pfp.getFilterParameterId().equals(paramReq.getId()) &&
                            paramReq.getFilterParameterValueIds().contains(pfp.getFilterParameterValueId()));

            if (!matchFound) {
                return false;
            }
        }
        return true;
    }

    private List<FilterParameterDto> getFilterParametersForProducts(List<Product> products, Set<String> selectedValueIds) {
        if (products.isEmpty()) {
            return List.of();
        }

        Map<String, Set<String>> filterParamToValuesMap = new HashMap<>();

        for (Product product : products) {
            if (product.getProductFilterParameters() != null) {
                for (ProductFilterParameter param : product.getProductFilterParameters()) {
                    if (param.getFilterParameterId() != null && param.getFilterParameterValueId() != null) {
                        filterParamToValuesMap
                                .computeIfAbsent(param.getFilterParameterId(), k -> new HashSet<>())
                                .add(param.getFilterParameterValueId());
                    }
                }
            }
        }

        List<FilterParameterDto> result = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : filterParamToValuesMap.entrySet()) {
            String filterParamId = entry.getKey();
            Set<String> valueIds = entry.getValue();

            Optional<FilterParameter> filterParameterOpt = filterParameterRepository.findById(filterParamId);
            if (filterParameterOpt.isPresent()) {
                FilterParameter filterParameter = filterParameterOpt.get();
                FilterParameterDto dto = filterParameterMapper.toDto(filterParameter);
                List<FilterParameterValueDto> valueDtos = new ArrayList<>();

                for (String valueId : valueIds) {
                    Optional<FilterParameterValue> valueOpt = filterParameterValueRepository.findById(valueId);
                    if (valueOpt.isPresent()) {
                        FilterParameterValue value = valueOpt.get();
                        FilterParameterValueDto valueDto = filterParameterValueMapper.toDto(value);
                        valueDto.setSelected(selectedValueIds.contains(valueId));
                        valueDtos.add(valueDto);
                    }
                }

                valueDtos.sort(Comparator.comparing(FilterParameterValueDto::getName, Comparator.nullsLast(Comparator.naturalOrder())));

                dto.setValues(valueDtos);
                result.add(dto);
            }
        }

        result.sort(Comparator.comparing(FilterParameterDto::getName, Comparator.nullsLast(Comparator.naturalOrder())));

        return result;
    }

    // Additional Category Search logic needed by CategoryAdminController which used to use ProductSearchEngine
    public Page<Product> findByCategoryIds(String categoryId, Pageable pageable) {
        // Implement logic to find products by categoryId
        Set<String> categoryIds = getAllCategoryIdsIncludingSubcategories(categoryId);
        List<Product> filtered = memoryCache.values().stream()
                .filter(p -> p.getCategoryIds() != null && !Collections.disjoint(p.getCategoryIds(), categoryIds))
                .collect(Collectors.toCollection(ArrayList::new));

        if (pageable.getSort().isSorted()) {
            sort(filtered, pageable.getSort());
        } else {
            filtered.sort(Comparator.comparing(Product::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        }

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filtered, pageable, filtered.size());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());

        if (start > filtered.size()) {
             return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    public Optional<Product> findBySlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        return memoryCache.values().stream()
                .filter(p -> slug.equals(p.getSlug()))
                .findFirst();
    }

    public boolean existsBySlug(String slug, String excludeId) {
        if (slug == null) {
            return false;
        }
        return memoryCache.values().stream()
                .anyMatch(p -> slug.equals(p.getSlug()) && (excludeId == null || !p.getId().equals(excludeId)));
    }
}
