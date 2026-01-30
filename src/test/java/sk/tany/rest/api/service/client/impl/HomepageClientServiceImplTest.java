package sk.tany.rest.api.service.client.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.homepage.HomepageGrid;
import sk.tany.rest.api.domain.homepage.HomepageGridRepository;
import sk.tany.rest.api.domain.homepage.SortField;
import sk.tany.rest.api.domain.homepage.SortOrder;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.client.homepage.HomepageGridResponse;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.client.ReviewClientService;
import sk.tany.rest.api.service.client.WishlistClientService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomepageClientServiceImplTest {

    @Mock
    private HomepageGridRepository homepageGridRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ReviewClientService reviewClientService;
    @Mock
    private WishlistClientService wishlistClientService;
    @Mock
    private ProductSearchEngine productSearchEngine;

    @InjectMocks
    private HomepageClientServiceImpl homepageClientService;

    @Test
    void getHomepageGrids_withBrandId_shouldReturnProducts() {
        HomepageGrid grid = new HomepageGrid();
        grid.setId("grid1");
        grid.setBrandId("brand1");
        grid.setSortField(SortField.CREATED_DATE);
        grid.setSortOrder(SortOrder.DESC);
        grid.setResultCount(5);

        Product p1 = new Product();
        p1.setId("p1");
        p1.setBrandId("brand1");
        p1.setActive(true);
        p1.setQuantity(10);
        p1.setCreateDate(Instant.now().minusSeconds(100));

        Product p2 = new Product();
        p2.setId("p2");
        p2.setBrandId("brand1");
        p2.setActive(true);
        p2.setQuantity(10);
        p2.setCreateDate(Instant.now());

        when(homepageGridRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(grid)));
        // Using any(ProductFilter.class) to match call
        when(productRepository.search(any(ProductFilter.class), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(p1, p2)));

        when(productMapper.toClientDto(any(Product.class))).thenReturn(new ProductClientDto());
        when(reviewClientService.getProductRatings(any())).thenReturn(Collections.emptyMap());
        when(wishlistClientService.getWishlistProductIds()).thenReturn(Collections.emptyList());

        HomepageGridResponse response = homepageClientService.getHomepageGrids();

        assertNotNull(response);
        assertEquals(1, response.getHomepageGrids().size());
        assertEquals("grid1", response.getHomepageGrids().get(0).getId());
        assertEquals(2, response.getHomepageGrids().get(0).getProducts().size());
    }

    @Test
    void getHomepageGrids_withCategoryId_shouldReturnProducts() {
        HomepageGrid grid = new HomepageGrid();
        grid.setId("grid2");
        grid.setCategoryId("cat1");
        grid.setResultCount(5);

        Product p1 = new Product();
        p1.setId("p1");
        p1.setCategoryIds(List.of("cat1"));
        p1.setActive(true);
        p1.setQuantity(10);

        when(homepageGridRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(grid)));
        when(productRepository.findByCategoryIds(eq("cat1"), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(p1)));

        when(productMapper.toClientDto(any(Product.class))).thenReturn(new ProductClientDto());
        when(reviewClientService.getProductRatings(any())).thenReturn(Collections.emptyMap());
        when(wishlistClientService.getWishlistProductIds()).thenReturn(Collections.emptyList());

        HomepageGridResponse response = homepageClientService.getHomepageGrids();

        assertNotNull(response);
        assertEquals(1, response.getHomepageGrids().size());
        assertEquals("grid2", response.getHomepageGrids().get(0).getId());
        assertEquals(1, response.getHomepageGrids().get(0).getProducts().size());
    }

    @Test
    void getHomepageGrids_withProductIds_shouldReturnProducts() {
        HomepageGrid grid = new HomepageGrid();
        grid.setId("grid3");
        grid.setProductIds(List.of("p1", "p2"));
        grid.setSortField(SortField.UPDATED_DATE);
        grid.setSortOrder(SortOrder.ASC);
        grid.setResultCount(5);

        Product p1 = new Product();
        p1.setId("p1");
        p1.setActive(true);
        p1.setQuantity(10);
        p1.setUpdateDate(Instant.now().minusSeconds(50));

        Product p2 = new Product();
        p2.setId("p2");
        p2.setActive(true);
        p2.setQuantity(10);
        p2.setUpdateDate(Instant.now());

        when(homepageGridRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(grid)));
        when(productRepository.findAllById(List.of("p1", "p2"))).thenReturn(List.of(p1, p2));

        when(productMapper.toClientDto(any(Product.class))).thenReturn(new ProductClientDto());
        when(reviewClientService.getProductRatings(any())).thenReturn(Collections.emptyMap());
        when(wishlistClientService.getWishlistProductIds()).thenReturn(Collections.emptyList());

        HomepageGridResponse response = homepageClientService.getHomepageGrids();

        assertNotNull(response);
        assertEquals(1, response.getHomepageGrids().size());
        assertEquals("grid3", response.getHomepageGrids().get(0).getId());
        assertEquals(2, response.getHomepageGrids().get(0).getProducts().size());
    }

    @Test
    void getHomepageGrids_shouldFilterOutProductsWithZeroQuantity() {
        HomepageGrid grid = new HomepageGrid();
        grid.setId("grid4");
        grid.setBrandId("brand1");
        grid.setResultCount(5);

        Product p1 = new Product();
        p1.setId("p1");
        p1.setBrandId("brand1");
        p1.setActive(true);
        p1.setQuantity(10); // Should be included

        Product p2 = new Product();
        p2.setId("p2");
        p2.setBrandId("brand1");
        p2.setActive(true);
        p2.setQuantity(0); // Should be filtered out

        when(homepageGridRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(grid)));
        when(productRepository.search(any(ProductFilter.class), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(p1, p2)));

        when(productMapper.toClientDto(any(Product.class))).thenReturn(new ProductClientDto());
        when(reviewClientService.getProductRatings(any())).thenReturn(Collections.emptyMap());
        when(wishlistClientService.getWishlistProductIds()).thenReturn(Collections.emptyList());

        HomepageGridResponse response = homepageClientService.getHomepageGrids();

        assertNotNull(response);
        assertEquals(1, response.getHomepageGrids().size());
        assertEquals(1, response.getHomepageGrids().get(0).getProducts().size());
    }
}
