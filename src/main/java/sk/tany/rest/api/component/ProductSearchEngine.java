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
    private final FilterParameterMapper filterParameterMapper;
    private final FilterParameterValueMapper filterParameterValueMapper;

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private final List<Product> cachedProducts = new CopyOnWriteArrayList<>();
    private final Map<String, FilterParameter> cachedFilterParameters = new ConcurrentHashMap<>();
    private final Map<String, FilterParameterValue> cachedFilterParameterValues = new ConcurrentHashMap<>();

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

        // We return the full list of filters for the category (facets), marking selected ones.
        // We do NOT restrict the list of available filters based on the selection (multi-select friendly).
        return getFilterParametersForProducts(productsInCategory, selectedValueIds);
    }

    public List<Product> filterProducts(CategoryFilterRequest request) {
        return cachedProducts.stream()
                .filter(p -> matchesFilter(p, request))
                .toList();
    }

    public List<sk.tany.rest.api.dto.ClientFilterParameterDto> getClientFilterParameters(List<Product> baseProducts, List<Product> availableProducts, CategoryFilterRequest request) {
        if (baseProducts.isEmpty()) {
            return List.of();
        }

        Set<String> availableValueIds = new HashSet<>();
        for (Product product : availableProducts) {
            if (product.getProductFilterParameters() != null) {
                for (ProductFilterParameter param : product.getProductFilterParameters()) {
                    if (param.getFilterParameterValueId() != null) {
                        availableValueIds.add(param.getFilterParameterValueId());
                    }
                }
            }
        }

        Set<String> selectedValueIds = new HashSet<>();
        if (request != null && request.getFilterParameters() != null) {
            for (FilterParameterRequest param : request.getFilterParameters()) {
                if (param.getFilterParameterValueIds() != null) {
                    selectedValueIds.addAll(param.getFilterParameterValueIds());
                }
            }
        }

        Map<String, Set<String>> filterParamToValuesMap = new HashMap<>();
        for (Product product : baseProducts) {
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

        List<sk.tany.rest.api.dto.ClientFilterParameterDto> result = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : filterParamToValuesMap.entrySet()) {
            String filterParamId = entry.getKey();
            Set<String> valueIds = entry.getValue();

            FilterParameter filterParameter = cachedFilterParameters.get(filterParamId);
            if (filterParameter != null) {
                sk.tany.rest.api.dto.ClientFilterParameterDto dto = new sk.tany.rest.api.dto.ClientFilterParameterDto();
                dto.setId(filterParameter.getId());
                dto.setName(filterParameter.getName());
                dto.setType(filterParameter.getType());
                dto.setActive(filterParameter.getActive());
                dto.setFilterParameterValueIds(new ArrayList<>(valueIds));

                List<sk.tany.rest.api.dto.ClientFilterParameterValueDto> valueDtos = new ArrayList<>();
                boolean anyValueAvailable = false;

                for (String valueId : valueIds) {
                    FilterParameterValue value = cachedFilterParameterValues.get(valueId);
                    if (value != null) {
                        sk.tany.rest.api.dto.ClientFilterParameterValueDto valueDto = new sk.tany.rest.api.dto.ClientFilterParameterValueDto();
                        valueDto.setId(value.getId());
                        valueDto.setFilterParameterId(value.getFilterParameterId());
                        valueDto.setName(value.getName());
                        valueDto.setActive(value.getActive());

                        boolean isSelected = selectedValueIds.contains(valueId);
                        boolean isAvailable = availableValueIds.contains(valueId);

                        valueDto.setSelected(isSelected);
                        valueDto.setAvailable(isAvailable);

                        if (isAvailable) {
                            anyValueAvailable = true;
                        }

                        valueDtos.add(valueDto);
                    }
                }

                valueDtos.sort(Comparator.comparing(sk.tany.rest.api.dto.ClientFilterParameterValueDto::getName, Comparator.nullsLast(Comparator.naturalOrder())));
                dto.setValues(valueDtos);
                dto.setAvailable(anyValueAvailable);

                result.add(dto);
            }
        }

        result.sort(Comparator.comparing(sk.tany.rest.api.dto.ClientFilterParameterDto::getName, Comparator.nullsLast(Comparator.naturalOrder())));

        return result;
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
