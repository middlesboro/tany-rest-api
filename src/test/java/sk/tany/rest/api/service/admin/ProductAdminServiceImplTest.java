package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.common.ImageService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductAdminServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductSearchEngine productSearchEngine;
    @Mock
    private ImageService imageService;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductAdminServiceImpl productAdminService;

    @Test
    void recalculateReviewStatistics_shouldSetZero_whenNoReviews() {
        String productId = "p1";
        ProductDto dto = new ProductDto();
        dto.setId(productId);
        Product product = new Product();
        product.setId(productId);

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(reviewRepository.findAllByProductId(productId)).thenReturn(Collections.emptyList());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(any(Product.class))).thenReturn(dto);

        productAdminService.update(productId, dto);

        assertThat(product.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(product.getReviewsCount()).isEqualTo(0);
    }

    @Test
    void recalculateReviewStatistics_shouldCalculateCorrectly_whenActiveReviewsExist() {
        String productId = "p1";
        ProductDto dto = new ProductDto();
        dto.setId(productId);
        Product product = new Product();
        product.setId(productId);

        Review r1 = new Review();
        r1.setRating(5);
        r1.setActive(true);
        Review r2 = new Review();
        r2.setRating(4);
        r2.setActive(true);

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(reviewRepository.findAllByProductId(productId)).thenReturn(List.of(r1, r2));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(any(Product.class))).thenReturn(dto);

        productAdminService.update(productId, dto);

        assertThat(product.getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
        assertThat(product.getReviewsCount()).isEqualTo(2);
    }

    @Test
    void recalculateReviewStatistics_shouldIgnoreInactiveReviews() {
        String productId = "p1";
        ProductDto dto = new ProductDto();
        dto.setId(productId);
        Product product = new Product();
        product.setId(productId);

        Review r1 = new Review();
        r1.setRating(5);
        r1.setActive(true);
        Review r2 = new Review();
        r2.setRating(1);
        r2.setActive(false);

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(reviewRepository.findAllByProductId(productId)).thenReturn(List.of(r1, r2));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(any(Product.class))).thenReturn(dto);

        productAdminService.update(productId, dto);

        assertThat(product.getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(product.getReviewsCount()).isEqualTo(1);
    }
}
