package sk.tany.rest.api.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductFilterParameter;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.FilterParameterRequest;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;

import java.util.*;
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
    private final FilterParameterMapper filterParameterMapper;
    private final FilterParameterValueMapper filterParameterValueMapper;

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private final List<Product> cachedProducts = new CopyOnWriteArrayList<>();
    private final Map<String, FilterParameter> cachedFilterParameters = new ConcurrentHashMap<>();
    private final Map<String, FilterParameterValue> cachedFilterParameterValues = new ConcurrentHashMap<>();
    private final Map<String, Integer> cachedProductSales = new ConcurrentHashMap<>();

    private static final int MAX_EDIT_DISTANCE = 2;

    @EventListener(ApplicationReadyEvent.class)
    public void loadProducts() {
        log.info("Loading products into search engine...");
        cachedProducts.clear();
        cachedProducts.addAll(productRepository.findAll());
        log.info("Loaded {} products into search engine.", cachedProducts.size());

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
            .collect(Collectors.toList());
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

        List<Product> productsInCategory = cachedProducts.stream()
                .filter(p -> p.getCategoryIds() != null && p.getCategoryIds().contains(categoryId))
                .toList();

        return getFilterParametersForProducts(productsInCategory, Collections.emptySet());
    }

    public List<Product> search(String categoryId, CategoryFilterRequest request) {
        if (categoryId == null) {
            return List.of();
        }
        return cachedProducts.stream()
                .filter(p -> p.getCategoryIds() != null && p.getCategoryIds().contains(categoryId))
                .filter(p -> matchesFilter(p, request))
                .toList();
    }

    public List<FilterParameterDto> getFilterParametersForCategoryWithFilter(String categoryId, CategoryFilterRequest filterRequest) {
        if (categoryId == null) {
            return List.of();
        }

        List<Product> productsInCategory = cachedProducts.stream()
                .filter(p -> p.getCategoryIds() != null && p.getCategoryIds().contains(categoryId))
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

    private CategoryFilterRequest createRequestExcludingFacet(CategoryFilterRequest original, String facetIdToExclude) {
        if (original == null || original.getFilterParameters() == null) {
            return new CategoryFilterRequest();
        }
        CategoryFilterRequest newRequest = new CategoryFilterRequest();
        newRequest.setFilterParameters(original.getFilterParameters().stream()
                .filter(param -> !param.getId().equals(facetIdToExclude))
                .collect(Collectors.toList()));
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
