package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.homepage.HomepageGrid;
import sk.tany.rest.api.domain.homepage.HomepageGridRepository;
import sk.tany.rest.api.domain.homepage.SortField;
import sk.tany.rest.api.domain.homepage.SortOrder;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.client.homepage.HomepageGridDto;
import sk.tany.rest.api.dto.client.homepage.HomepageGridResponse;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.dto.client.review.ProductRatingDto;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.client.HomepageClientService;
import sk.tany.rest.api.service.client.ReviewClientService;
import sk.tany.rest.api.service.client.WishlistClientService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HomepageClientServiceImpl implements HomepageClientService {

    private final HomepageGridRepository homepageGridRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ReviewClientService reviewClientService;
    private final WishlistClientService wishlistClientService;
    private final ProductSearchEngine productSearchEngine;

    @Override
    public HomepageGridResponse getHomepageGrids() {
        List<HomepageGrid> grids = homepageGridRepository.findAll();
        List<HomepageGridDto> gridDtos = grids.stream()
                .map(this::processGrid)
                .collect(Collectors.toList());

        return new HomepageGridResponse(gridDtos);
    }

    private HomepageGridDto processGrid(HomepageGrid grid) {
        Stream<Product> productStream = Stream.empty();

        if (StringUtils.hasText(grid.getBrandId())) {
            ProductFilter filter = new ProductFilter(null, null, null, grid.getBrandId(), null, null, null, null);
            productStream = productRepository.search(filter, Pageable.unpaged()).getContent().stream();
        } else if (StringUtils.hasText(grid.getCategoryId())) {
            productStream = productRepository.findByCategoryIds(grid.getCategoryId(), Pageable.unpaged()).getContent().stream();
        } else if (grid.getProductIds() != null && !grid.getProductIds().isEmpty()) {
            productStream = productRepository.findAllById(grid.getProductIds()).stream();
        }

        // Filter active
        productStream = productStream.filter(Product::isActive);

        // Sort
        Comparator<Product> comparator = getComparator(grid.getSortField(), grid.getSortOrder());
        if (comparator != null) {
            productStream = productStream.sorted(comparator);
        }

        // Limit
        if (grid.getResultCount() != null && grid.getResultCount() > 0) {
            productStream = productStream.limit(grid.getResultCount());
        }

        List<Product> products = productStream.collect(Collectors.toList());
        List<ProductClientDto> productDtos = mapToEnhancedDtos(products);

        HomepageGridDto dto = new HomepageGridDto();
        dto.setId(grid.getId());
        dto.setProducts(productDtos);
        return dto;
    }

    private Comparator<Product> getComparator(SortField field, SortOrder order) {
        if (field == null) return null;

        Comparator<Product> comparator = null;
        switch (field) {
            case CREATED_DATE:
                comparator = Comparator.comparing(Product::getCreateDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case UPDATED_DATE:
                comparator = Comparator.comparing(Product::getUpdateDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }

        if (comparator != null && order == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private List<ProductClientDto> mapToEnhancedDtos(List<Product> products) {
        if (products.isEmpty()) return Collections.emptyList();

        List<String> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        Map<String, ProductRatingDto> ratings = reviewClientService.getProductRatings(productIds);
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());

        return products.stream().map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            ProductRatingDto rating = ratings.getOrDefault(product.getId(), new ProductRatingDto(BigDecimal.ZERO, 0));
            dto.setAverageRating(rating.getAverageRating());
            dto.setReviewsCount(rating.getReviewsCount());
            return dto;
        }).collect(Collectors.toList());
    }
}
