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
        return productRepository.findAll(pageable).map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            return dto;
        });
    }

    @Override
    public Optional<ProductClientDto> findById(String id) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        return productRepository.findById(id).map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            return dto;
        });
    }

    @Override
    public Page<ProductClientDto> search(String categoryId, Pageable pageable) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        return productRepository.findByCategoryIds(categoryId, pageable).map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            return dto;
        });
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
            pageContent = products.subList(start, end).stream()
                    .map(product -> {
                        ProductClientDto dto = productMapper.toClientDto(product);
                        dto.setProductLabels(productSearchEngine.getProductLabels(product.getProductLabelIds()));
                        dto.setInWishlist(wishlistProductIds.contains(product.getId()));
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
        return productRepository.findAllById(ids).stream().map(product -> {
            ProductClientDto dto = productMapper.toClientDto(product);
            dto.setInWishlist(wishlistProductIds.contains(product.getId()));
            return dto;
        }).toList();
    }

    @Override
    public java.util.List<ProductClientDto> searchProducts(String query) {
        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        return productSearchEngine.searchAndSort(query)
                .stream()
                .map(product -> {
                    ProductClientDto dto = productMapper.toClientDto(product);
                    dto.setInWishlist(wishlistProductIds.contains(product.getId()));
                    return dto;
                })
                .toList();
    }

    @Override
    public java.util.List<ProductClientDto> getRelatedProducts(String productId) {
        java.util.List<String> relatedIds = productEmbeddingService.findRelatedProducts(productId);
        if (relatedIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        Set<String> wishlistProductIds = new HashSet<>(wishlistClientService.getWishlistProductIds());
        return productRepository.findAllById(relatedIds).stream()
                .map(product -> {
                    ProductClientDto dto = productMapper.toClientDto(product);
                    dto.setInWishlist(wishlistProductIds.contains(product.getId()));
                    return dto;
                })
                .toList();
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
