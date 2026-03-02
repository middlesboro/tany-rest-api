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
        product.setAverageRating(BigDecimal.valueOf(4.5));
        product.setReviewsCount(10);

        when(wishlistClientService.getWishlistProductIds()).thenReturn(Collections.emptyList());
        when(productSearchEngine.findAll(any(Pageable.class), org.mockito.ArgumentMatchers.anyBoolean())).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toClientDto(product)).thenReturn(productClientDto);

        Page<ProductClientDto> result = service.findAll(Pageable.unpaged());

        assertEquals(1, result.getContent().size());
        ProductClientDto dto = result.getContent().getFirst();
        assertEquals(BigDecimal.valueOf(4.5), dto.getAverageRating());
        assertEquals(10, dto.getReviewsCount());
    }
}
