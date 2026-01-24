package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.dto.client.product.ProductClientSearchDto;
import sk.tany.rest.api.dto.client.review.ProductRatingDto;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.exception.ProductException;
import sk.tany.rest.api.service.common.ProductEmbeddingService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ProductClientServiceImpl implements ProductClientService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductSearchEngine productSearchEngine;
    private final WishlistClientService wishlistClientService;
    private final ProductEmbeddingService productEmbeddingService;
    private final ReviewClientService reviewClientService;

    @Override
    public Page<ProductClientDto> findAll(Pageable pageable) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        Page<Product> products = productRepository.findAll(pageable);
        return mapToEnhancedDtos(products, wishlistProductIds);
    }

    @Override
    public Optional<ProductClientDto> findById(String id) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        return productRepository.findById(id).map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            ProductRatingDto rating = reviewClientService.getProductRating(product.getId());
            dto.setAverageRating(rating.getAverageRating());
            dto.setReviewsCount(rating.getReviewsCount());
            return dto;
        });
    }

    @Override
    public Page<ProductClientDto> search(String categoryId, Pageable pageable) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        Page<Product> products = productRepository.findByCategoryIds(categoryId, pageable);
        return mapToEnhancedDtos(products, wishlistProductIds);
    }

    @Override
    public ProductClientSearchDto search(String categoryId, sk.tany.rest.api.dto.request.CategoryFilterRequest request, Pageable pageable) {
        java.util.List<Product> products = productSearchEngine.search(categoryId, request);
        java.util.List<sk.tany.rest.api.dto.FilterParameterDto> filters = productSearchEngine.getFilterParametersForCategoryWithFilter(categoryId, request);
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        java.util.List<ProductClientDto> pageContent;
        if (start > products.size()) {
            pageContent = java.util.Collections.emptyList();
        } else {
            List<Product> subList = products.subList(start, end);
            List<String> productIds = subList.stream().map(Product::getId).toList();
            Map<String, ProductRatingDto> ratings = reviewClientService.getProductRatings(productIds);

            pageContent = subList.stream()
                    .map(product -> {
                        ProductClientDto dto = productMapper.toClientDto(product);
                        dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
                        dto.setInWishlist(wishlistProductIds.contains(product.getId()));
                        ProductRatingDto rating = ratings.getOrDefault(product.getId(), new ProductRatingDto(BigDecimal.ZERO, 0));
                        dto.setAverageRating(rating.getAverageRating());
                        dto.setReviewsCount(rating.getReviewsCount());
                        return dto;
                    })
                    .toList();
        }
        Page<ProductClientDto> productsPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, products.size());

        ProductClientSearchDto response = new ProductClientSearchDto();
        response.setProducts(productsPage);
        response.setFilterParameters(filters);
        return response;
    }

    @Override
    public java.util.List<ProductClientDto> findAllByIds(Iterable<String> ids) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        List<Product> products = productRepository.findAllById(ids);
        return mapToEnhancedDtos(products, wishlistProductIds);
    }

    @Override
    public java.util.List<ProductClientDto> searchProducts(String query) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        List<Product> products = productSearchEngine.searchAndSort(query);
        return mapToEnhancedDtos(products, wishlistProductIds);
    }

    @Override
    public java.util.List<ProductClientDto> getRelatedProducts(String productId) {
        java.util.List<String> relatedIds = productEmbeddingService.findRelatedProducts(productId);
        if (relatedIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        List<Product> products = productRepository.findAllById(relatedIds);
        return mapToEnhancedDtos(products, wishlistProductIds);
    }

    private Page<ProductClientDto> mapToEnhancedDtos(Page<Product> productsPage, Set<String> wishlistProductIds) {
        List<String> productIds = productsPage.getContent().stream().map(Product::getId).toList();
        Map<String, ProductRatingDto> ratings = reviewClientService.getProductRatings(productIds);

        return productsPage.map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            ProductRatingDto rating = ratings.getOrDefault(product.getId(), new ProductRatingDto(BigDecimal.ZERO, 0));
            dto.setAverageRating(rating.getAverageRating());
            dto.setReviewsCount(rating.getReviewsCount());
            return dto;
        });
    }

    private List<ProductClientDto> mapToEnhancedDtos(List<Product> products, Set<String> wishlistProductIds) {
        if (products.isEmpty()) return java.util.Collections.emptyList();
        List<String> productIds = products.stream().map(Product::getId).toList();
        Map<String, ProductRatingDto> ratings = reviewClientService.getProductRatings(productIds);

        return products.stream().map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            ProductRatingDto rating = ratings.getOrDefault(product.getId(), new ProductRatingDto(BigDecimal.ZERO, 0));
            dto.setAverageRating(rating.getAverageRating());
            dto.setReviewsCount(rating.getReviewsCount());
            return dto;
        }).toList();
    }

    @Override
    public void updateProductStock(String productId, Integer quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException.NotFound("Product not found"));

        int currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
        int newQuantity = currentQuantity - quantityChange;
        if (newQuantity < 0) {
            throw new ProductException.BadRequest("Not enough stock for product: " + product.getTitle());
        }
        product.setQuantity(newQuantity);
        productRepository.save(product);
        productSearchEngine.updateProduct(product);
    }
}
