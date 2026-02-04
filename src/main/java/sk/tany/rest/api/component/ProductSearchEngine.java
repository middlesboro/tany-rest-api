package sk.tany.rest.api.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductFilterParameter;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.domain.productlabel.ProductLabel;
import sk.tany.rest.api.domain.productlabel.ProductLabelRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.FilterParameterRequest;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;
import sk.tany.rest.api.mapper.ProductLabelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchEngine {

    private final ProductRepository productRepository;
    private final FilterParameterRepository filterParameterRepository;
    private final FilterParameterValueRepository filterParameterValueRepository;
    private final ProductSalesRepository productSalesRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductLabelRepository productLabelRepository;
    private final FilterParameterMapper filterParameterMapper;
    private final FilterParameterValueMapper filterParameterValueMapper;
    private final ProductLabelMapper productLabelMapper;

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private final List<Product> cachedProducts = new CopyOnWriteArrayList<>();
    private final Map<String, FilterParameter> cachedFilterParameters = new ConcurrentHashMap<>();
    private final Map<String, FilterParameterValue> cachedFilterParameterValues = new ConcurrentHashMap<>();
    private final Map<String, Integer> cachedProductSales = new ConcurrentHashMap<>();
    private final Map<String, Category> cachedCategories = new ConcurrentHashMap<>();
    private final Map<String, List<String>> cachedCategoryChildren = new ConcurrentHashMap<>();
    private final Map<String, ProductLabel> cachedProductLabels = new ConcurrentHashMap<>();
    private final Map<String, Brand> cachedBrands = new ConcurrentHashMap<>();

    private static final int MAX_EDIT_DISTANCE = 2;

    @EventListener(ApplicationReadyEvent.class)
    public void loadProducts() {
        log.info("Loading products into search engine...");
        cachedProducts.clear();
        cachedProducts.addAll(productRepository.findAll());
        log.info("Loaded {} products into search engine.", cachedProducts.size());

        log.info("Loading categories into search engine...");
        cachedCategories.clear();
        cachedCategoryChildren.clear();
        List<Category> allCategories = categoryRepository.findAll();
        for (Category category : allCategories) {
            cachedCategories.put(category.getId(), category);
            if (category.getParentId() != null) {
                cachedCategoryChildren.computeIfAbsent(category.getParentId(), k -> new ArrayList<>()).add(category.getId());
            }
        }
        log.info("Loaded {} categories into search engine.", cachedCategories.size());

        log.info("Loading filter parameters into search engine...");
        cachedFilterParameters.clear();
        cachedFilterParameters.putAll(filterParameterRepository.findAll().stream()
                .collect(Collectors.toMap(FilterParameter::getId, Function.identity())));
        log.info("Loaded {} filter parameters into search engine.", cachedFilterParameters.size());

        log.info("Loading filter parameter values into search engine...");
        cachedFilterParameterValues.clear();
        cachedFilterParameterValues.putAll(filterParameterValueRepository.findAll().stream()
                .collect(Collectors.toMap(FilterParameterValue::getId, Function.identity())));
        log.info("Loaded {} filter parameter values into search engine.", cachedFilterParameterValues.size());

        log.info("Loading product sales into search engine...");
        cachedProductSales.clear();
        cachedProductSales.putAll(productSalesRepository.findAll().stream()
                .collect(Collectors.toMap(ProductSales::getProductId, ProductSales::getSalesCount)));
        log.info("Loaded {} product sales into search engine.", cachedProductSales.size());

        log.info("Loading product labels into search engine...");
        cachedProductLabels.clear();
        cachedProductLabels.putAll(productLabelRepository.findAll().stream()
                .collect(Collectors.toMap(ProductLabel::getId, Function.identity())));
        log.info("Loaded {} product labels into search engine.", cachedProductLabels.size());

        log.info("Loading brands into search engine...");
        cachedBrands.clear();
        cachedBrands.putAll(brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, Function.identity())));
        log.info("Loaded {} brands into search engine.", cachedBrands.size());
    }

    public List<ProductLabelDto> getProductLabels(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .map(cachedProductLabels::get)
                .filter(Objects::nonNull)
                .filter(ProductLabel::isActive)
                .map(productLabelMapper::toDto)
                .toList();
    }

    public void updateSalesCount(String productId, int count) {
        if (productId != null) {
            cachedProductSales.put(productId, count);
        }
    }

    public Integer getSalesCount(String productId) {
        return cachedProductSales.getOrDefault(productId, 0);
    }

    public void addProduct(Product product) {
        if (product.getId() != null) {
            removeProduct(product.getId());
        }
        cachedProducts.add(product);
    }

    public void updateProduct(Product product) {
        addProduct(product);
    }

    public void removeProduct(String productId) {
        if (productId != null) {
            cachedProducts.removeIf(p -> productId.equals(p.getId()));
        }
    }

    public void addCategory(Category category) {
        updateCategory(category);
    }

    public void updateCategory(Category category) {
        if (category == null || category.getId() == null) return;

        Category oldCategory = cachedCategories.put(category.getId(), category);

        String oldParentId = (oldCategory != null) ? oldCategory.getParentId() : null;
        String newParentId = category.getParentId();

        if (!Objects.equals(oldParentId, newParentId)) {
            if (oldParentId != null) {
                List<String> children = cachedCategoryChildren.get(oldParentId);
                if (children != null) {
                    children.remove(category.getId());
                }
            }
            if (newParentId != null) {
                cachedCategoryChildren.computeIfAbsent(newParentId, k -> new ArrayList<>()).add(category.getId());
            }
        }
    }

    public void removeCategory(String categoryId) {
        if (categoryId != null) {
            Category category = cachedCategories.remove(categoryId);
            if (category != null && category.getParentId() != null) {
                List<String> children = cachedCategoryChildren.get(category.getParentId());
                if (children != null) {
                    children.remove(categoryId);
                }
            }
            cachedCategoryChildren.remove(categoryId);
        }
    }

    public Page<Category> searchCategories(String query, Pageable pageable) {
        String normalizedQuery = null;
        String[] queryWords = null;

        if (StringUtils.isNotBlank(query)) {
            normalizedQuery = StringUtils.stripAccents(query.toLowerCase()).trim();
            queryWords = normalizedQuery.split("\\s+");
        }

        final String finalNormalizedQuery = normalizedQuery;
        final String[] finalQueryWords = queryWords;

        List<Category> filteredCategories = cachedCategories.values().stream()
                .filter(c -> {
                    if (finalQueryWords != null) {
                        if (c.getTitle() == null) {
                            return false;
                        }
                        String normalizedName = StringUtils.stripAccents(c.getTitle().toLowerCase());
                        String[] nameWords = normalizedName.split("\\s+");

                        return Arrays.stream(finalQueryWords).allMatch(qWord ->
                                Arrays.stream(nameWords).anyMatch(nWord ->
                                        nWord.contains(qWord) || levenshtein.apply(nWord, qWord) <= MAX_EDIT_DISTANCE
                                )
                        );
                    }
                    return true;
                })
                .sorted((c1, c2) -> {
                    if (finalNormalizedQuery != null) {
                        Double score1 = calculateRelevance(c1.getTitle(), finalNormalizedQuery);
                        Double score2 = calculateRelevance(c2.getTitle(), finalNormalizedQuery);
                        return score2.compareTo(score1);
                    }
                    return StringUtils.compareIgnoreCase(c1.getTitle(), c2.getTitle());
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCategories.size());

        if (start > filteredCategories.size()) {
            return new PageImpl<>(List.of(), pageable, filteredCategories.size());
        }

        List<Category> pageContent = filteredCategories.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredCategories.size());
    }

    public void addFilterParameter(FilterParameter filterParameter) {
        if (filterParameter != null && filterParameter.getId() != null) {
            cachedFilterParameters.put(filterParameter.getId(), filterParameter);
        }
    }

    public void addFilterParameterValue(FilterParameterValue filterParameterValue) {
        if (filterParameterValue != null && filterParameterValue.getId() != null) {
            cachedFilterParameterValues.put(filterParameterValue.getId(), filterParameterValue);
        }
    }

    public List<Product> searchAndSort(String query) {
        if (StringUtils.isBlank(query)) {
            return List.of();
        }

        // 1. Normalizácia vstupu
        String normalizedQuery = StringUtils.stripAccents(query.toLowerCase()).trim();
        String[] queryWords = normalizedQuery.split("\\s+");

        return cachedProducts.stream()
            // 2. Filtrovanie (musí obsahovať aspoň niečo podobné všetkým slovám v dopyte)
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
            // 3. Výpočet skóre a zoradenie
            .sorted((p1, p2) -> {
                Double score1 = calculateRelevance(p1.getTitle(), normalizedQuery);
                Double score2 = calculateRelevance(p2.getTitle(), normalizedQuery);
                return score2.compareTo(score1); // Od najvyššieho skóre
            })
            .toList();
    }

    private Double calculateRelevance(String productName, String normalizedQuery) {
        if (productName == null) {
            return 0.0;
        }
        String normalizedName = StringUtils.stripAccents(productName.toLowerCase());
        // Jaro-Winkler vráti hodnotu medzi 0.0 a 1.0
        return jaroWinkler.apply(normalizedName, normalizedQuery);
    }

    public List<FilterParameterDto> getFilterParametersForCategory(String categoryId) {
        if (categoryId == null) {
            return List.of();
        }

        Set<String> categoryIds = getAllCategoryIdsIncludingSubcategories(categoryId);

        List<Product> productsInCategory = cachedProducts.stream()
                .filter(p -> p.getCategoryIds() != null && !Collections.disjoint(p.getCategoryIds(), categoryIds))
                .toList();

        return getFilterParametersForProducts(productsInCategory, Collections.emptySet());
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
            // Default sort if none specified, or you can leave it unsorted/default
            comparator = Comparator.comparing(Product::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        return cachedProducts.stream()
                .filter(p -> p.getCategoryIds() != null && !Collections.disjoint(p.getCategoryIds(), categoryIds))
                .filter(p -> matchesFilter(p, request))
                .sorted(comparator)
                .toList();
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

        List<Product> filteredProducts = cachedProducts.stream()
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
                .sorted((p1, p2) -> {
                    if (finalNormalizedQuery != null) {
                        Double score1 = calculateRelevance(p1.getTitle(), finalNormalizedQuery);
                        Double score2 = calculateRelevance(p2.getTitle(), finalNormalizedQuery);
                        return score2.compareTo(score1);
                    }
                    return 0; // Or default sort
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());

        if (start > filteredProducts.size()) {
             return new PageImpl<>(List.of(), pageable, filteredProducts.size());
        }

        List<Product> pageContent = filteredProducts.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredProducts.size());
    }

    public List<FilterParameterDto> getFilterParametersForCategoryWithFilter(String categoryId, CategoryFilterRequest filterRequest) {
        if (categoryId == null) {
            return List.of();
        }
        Set<String> categoryIds = getAllCategoryIdsIncludingSubcategories(categoryId);

        List<Product> productsInCategory = cachedProducts.stream()
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

        // Add BRAND filter
        FilterParameterDto brandFacet = new FilterParameterDto();
        brandFacet.setId("BRAND");
        brandFacet.setName("Brand");
        brandFacet.setType(sk.tany.rest.api.domain.filter.FilterParameterType.BRAND);
        brandFacet.setValues(new ArrayList<>());

        Set<String> presentBrandNames = productsInCategory.stream()
                .map(Product::getBrandId)
                .filter(Objects::nonNull)
                .map(cachedBrands::get)
                .filter(Objects::nonNull)
                .map(Brand::getName)
                .collect(Collectors.toSet());

        for (String brandName : presentBrandNames) {
            FilterParameterValueDto valueDto = new FilterParameterValueDto();
            valueDto.setId(brandName);
            valueDto.setName(brandName);
            valueDto.setSelected(selectedValueIds.contains(brandName));
            brandFacet.getValues().add(valueDto);
        }
        if (!brandFacet.getValues().isEmpty()) {
            brandFacet.getValues().sort(Comparator.comparing(FilterParameterValueDto::getName, Comparator.nullsLast(Comparator.naturalOrder())));
            allFacets.add(brandFacet);
        }

        // Add AVAILABILITY filter
        FilterParameterDto availabilityFacet = new FilterParameterDto();
        availabilityFacet.setId("AVAILABILITY");
        availabilityFacet.setName("Availability");
        availabilityFacet.setType(sk.tany.rest.api.domain.filter.FilterParameterType.AVAILABILITY);
        availabilityFacet.setValues(new ArrayList<>());

        FilterParameterValueDto onStock = new FilterParameterValueDto();
        onStock.setId("ON_STOCK");
        onStock.setName("ON_STOCK");
        onStock.setSelected(selectedValueIds.contains("ON_STOCK"));
        availabilityFacet.getValues().add(onStock);

        FilterParameterValueDto soldOut = new FilterParameterValueDto();
        soldOut.setId("SOLD_OUT");
        soldOut.setName("SOLD_OUT");
        soldOut.setSelected(selectedValueIds.contains("SOLD_OUT"));
        availabilityFacet.getValues().add(soldOut);

        allFacets.add(availabilityFacet);

        // Calculate availability
        for (FilterParameterDto facet : allFacets) {
            CategoryFilterRequest otherFiltersRequest = createRequestExcludingFacet(filterRequest, facet.getId());

            List<Product> productsMatchingOthers = productsInCategory.stream()
                    .filter(p -> matchesFilter(p, otherFiltersRequest))
                    .toList();

            if ("BRAND".equals(facet.getId())) {
                Set<String> availableBrandNames = productsMatchingOthers.stream()
                        .map(Product::getBrandId)
                        .filter(Objects::nonNull)
                        .map(cachedBrands::get)
                        .filter(Objects::nonNull)
                        .map(Brand::getName)
                        .collect(Collectors.toSet());

                for (FilterParameterValueDto valueDto : facet.getValues()) {
                    valueDto.setAvailable(availableBrandNames.contains(valueDto.getId()));
                }
            } else if ("AVAILABILITY".equals(facet.getId())) {
                boolean hasOnStock = productsMatchingOthers.stream().anyMatch(p -> p.getQuantity() != null && p.getQuantity() > 0);
                boolean hasSoldOut = productsMatchingOthers.stream().anyMatch(p -> p.getQuantity() == null || p.getQuantity() <= 0);

                for (FilterParameterValueDto valueDto : facet.getValues()) {
                    if ("ON_STOCK".equals(valueDto.getId())) {
                        valueDto.setAvailable(hasOnStock);
                    } else if ("SOLD_OUT".equals(valueDto.getId())) {
                        valueDto.setAvailable(hasSoldOut);
                    }
                }
            } else {
                Set<String> availableValuesForFacet = productsMatchingOthers.stream()
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
        }

        return allFacets;
    }

    private Set<String> getAllCategoryIdsIncludingSubcategories(String categoryId) {
        Set<String> result = new HashSet<>();
        if (categoryId != null) {
            collectCategoryIds(categoryId, result);
        }
        return result;
    }

    private void collectCategoryIds(String categoryId, Set<String> result) {
        if (result.contains(categoryId)) return; // prevent cycles
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

        for (FilterParameterRequest paramReq : filterRequest.getFilterParameters()) {
            if (paramReq.getFilterParameterValueIds() == null || paramReq.getFilterParameterValueIds().isEmpty()) {
                continue;
            }

            if ("BRAND".equals(paramReq.getId())) {
                String brandId = product.getBrandId();
                if (brandId == null) {
                    return false;
                }
                Brand brand = cachedBrands.get(brandId);
                if (brand == null || !paramReq.getFilterParameterValueIds().contains(brand.getName())) {
                    return false;
                }
            } else if ("AVAILABILITY".equals(paramReq.getId())) {
                boolean onStockSelected = paramReq.getFilterParameterValueIds().contains("ON_STOCK");
                boolean soldOutSelected = paramReq.getFilterParameterValueIds().contains("SOLD_OUT");

                boolean match = false;
                if (onStockSelected) {
                    if (product.getQuantity() != null && product.getQuantity() > 0) {
                        match = true;
                    }
                }
                if (soldOutSelected) {
                    if (product.getQuantity() == null || product.getQuantity() <= 0) {
                        match = true;
                    }
                }
                if (!match) {
                    return false;
                }
            } else {
                if (product.getProductFilterParameters() == null) {
                    return false;
                }
                boolean matchFound = product.getProductFilterParameters().stream()
                        .anyMatch(pfp -> pfp.getFilterParameterId() != null &&
                                pfp.getFilterParameterId().equals(paramReq.getId()) &&
                                paramReq.getFilterParameterValueIds().contains(pfp.getFilterParameterValueId()));

                if (!matchFound) {
                    return false;
                }
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

            FilterParameter filterParameter = cachedFilterParameters.get(filterParamId);
            if (filterParameter != null) {
                FilterParameterDto dto = filterParameterMapper.toDto(filterParameter);
                List<FilterParameterValueDto> valueDtos = new ArrayList<>();

                for (String valueId : valueIds) {
                    FilterParameterValue value = cachedFilterParameterValues.get(valueId);
                    if (value != null) {
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
}
