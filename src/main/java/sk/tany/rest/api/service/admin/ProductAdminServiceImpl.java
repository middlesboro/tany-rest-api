package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.common.ImageService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductAdminServiceImpl implements ProductAdminService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductSearchEngine productSearchEngine;
    private final ImageService imageService;
    private final ReviewRepository reviewRepository;

    @Override
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Override
    public Optional<ProductDto> findById(String id) {
        return productRepository.findById(id).map(productMapper::toDto);
    }

    @Override
    public ProductDto save(ProductDto productDto) {
        var product = productMapper.toEntity(productDto);
        recalculateReviewStatistics(product);
        var savedProduct = productRepository.save(product);
        productSearchEngine.addProduct(savedProduct);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto update(String id, ProductDto productDto) {
        productDto.setId(id);
        var product = productMapper.toEntity(productDto);
        recalculateReviewStatistics(product);
        var savedProduct = productRepository.save(product);
        productSearchEngine.updateProduct(savedProduct);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto patch(String id, sk.tany.rest.api.dto.admin.product.patch.ProductPatchRequest patchDto) {
        var product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        recalculateReviewStatistics(product);
        productMapper.updateEntityFromPatch(patchDto, product);
        var savedProduct = productRepository.save(product);
        productSearchEngine.updateProduct(savedProduct);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public void deleteById(String id) {
        var product = productRepository.findById(id);
        if (product.isPresent()) {
            var images = product.get().getImages();
            if (images != null) {
                images.forEach(imageService::delete);
            }
            productRepository.deleteById(id);
            productSearchEngine.removeProduct(id);
        }
    }

    @Override
    public Page<ProductDto> search(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryIds(categoryId, pageable).map(productMapper::toDto);
    }

    @Override
    public java.util.List<ProductDto> findAllByIds(Iterable<String> ids) {
        return productRepository.findAllById(ids).stream().map(productMapper::toDto).toList();
    }

    @Override
    public java.util.List<ProductDto> searchByQuery(String query) {
        return productSearchEngine.searchAndSort(query).stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public java.util.List<ProductDto> findAllByFilterParameterValueId(String filterParameterValueId) {
        return productRepository.findAllByProductFilterParametersFilterParameterValueId(filterParameterValueId).stream()
                .map(productMapper::toDto)
                .toList();
    }

    private void recalculateReviewStatistics(Product product) {
        if (product.getId() == null) {
            product.setAverageRating(BigDecimal.ZERO);
            product.setReviewsCount(0);
            return;
        }

        List<Review> reviews = reviewRepository.findAllByProductId(product.getId());
        List<Review> activeReviews = reviews.stream()
                .filter(Review::isActive)
                .toList();

        int count = activeReviews.size();
        if (count == 0) {
            product.setAverageRating(BigDecimal.ZERO);
            product.setReviewsCount(0);
            return;
        }

        double average = activeReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setAverageRating(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
        product.setReviewsCount(count);
    }
}
