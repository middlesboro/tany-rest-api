package sk.tany.rest.api.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.dto.client.review.ProductRatingDto;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.common.ProductEmbeddingService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductClientServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductSearchEngine productSearchEngine;
    @Mock
    private WishlistClientService wishlistClientService;
    @Mock
    private ProductEmbeddingService productEmbeddingService;
    @Mock
    private ReviewClientService reviewClientService;

    @InjectMocks
    private ProductClientServiceImpl service;

    private Product product;
    private ProductClientDto productClientDto;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("product1");

        productClientDto = new ProductClientDto();
        productClientDto.setId("product1");
    }

    @Test
    void findAll_shouldPopulateRating() {
        when(wishlistClientService.getWishlistProductIds()).thenReturn(Collections.emptyList());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toClientDto(product)).thenReturn(productClientDto);

        ProductRatingDto ratingDto = new ProductRatingDto(BigDecimal.valueOf(4.5), 10);
        when(reviewClientService.getProductRatings(List.of("product1"))).thenReturn(java.util.Map.of("product1", ratingDto));

        Page<ProductClientDto> result = service.findAll(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
        ProductClientDto dto = result.getContent().get(0);
        assertEquals(BigDecimal.valueOf(4.5), dto.getAverageRating());
        assertEquals(10, dto.getReviewsCount());
    }
}
