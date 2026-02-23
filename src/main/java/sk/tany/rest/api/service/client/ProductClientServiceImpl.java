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
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.exception.ProductException;
import sk.tany.rest.api.service.common.ProductEmbeddingService;

import java.math.BigDecimal;
import java.util.List;
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

    @Override
    public Page<ProductClientDto> findAll(Pageable pageable) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        Page<Product> products = productSearchEngine.findAll(pageable, true);
        return mapToEnhancedDtos(products, wishlistProductIds);
    }

    @Override
    public Optional<ProductClientDto> findById(String id) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        return productRepository.findById(id).map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            dto.setAverageRating(product.getAverageRating() != null ? product.getAverageRating() : BigDecimal.ZERO);
            dto.setReviewsCount(product.getReviewsCount() != null ? product.getReviewsCount() : 0);
            return dto;
        });
    }

    @Override
    public Optional<ProductClientDto> findBySlug(String slug) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        return productRepository.findBySlug(slug).map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            dto.setAverageRating(product.getAverageRating() != null ? product.getAverageRating() : BigDecimal.ZERO);
            dto.setReviewsCount(product.getReviewsCount() != null ? product.getReviewsCount() : 0);
            return dto;
        });
    }

    @Override
    public Page<ProductClientDto> search(String categoryId, Pageable pageable) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        Page<Product> products = productSearchEngine.findByCategoryIds(categoryId, pageable, true);
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

            pageContent = subList.stream()
                    .map(product -> {
                        ProductClientDto dto = productMapper.toClientDto(product);
                        dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
                        dto.setInWishlist(wishlistProductIds.contains(product.getId()));
                        dto.setAverageRating(product.getAverageRating() != null ? product.getAverageRating() : BigDecimal.ZERO);
                        dto.setReviewsCount(product.getReviewsCount() != null ? product.getReviewsCount() : 0);
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
        List<Product> products = productSearchEngine.searchAndSort(query, true);
        return mapToEnhancedDtos(products, wishlistProductIds);
    }

    @Override
    public java.util.List<ProductClientDto> getRelatedProducts(String productId) {
        java.util.List<ProductClientDto> relatedProducts = productEmbeddingService.findRelatedProducts(productId);
        if (relatedProducts.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        for (ProductClientDto dto : relatedProducts) {
            dto.setInWishlist(wishlistProductIds.contains(dto.getId()));
        }
        return relatedProducts;
    }

    private Page<ProductClientDto> mapToEnhancedDtos(Page<Product> productsPage, Set<String> wishlistProductIds) {
        return productsPage.map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            dto.setAverageRating(product.getAverageRating() != null ? product.getAverageRating() : BigDecimal.ZERO);
            dto.setReviewsCount(product.getReviewsCount() != null ? product.getReviewsCount() : 0);
            return dto;
        });
    }

    private List<ProductClientDto> mapToEnhancedDtos(List<Product> products, Set<String> wishlistProductIds) {
        if (products.isEmpty()) return java.util.Collections.emptyList();

        return products.stream().map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            dto.setAverageRating(product.getAverageRating() != null ? product.getAverageRating() : BigDecimal.ZERO);
            dto.setReviewsCount(product.getReviewsCount() != null ? product.getReviewsCount() : 0);
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
