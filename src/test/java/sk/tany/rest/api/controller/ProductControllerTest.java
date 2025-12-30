package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.service.ProductService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProducts_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductDto productDto = new ProductDto();
        productDto.setTitle("Test Product");
        Page<ProductDto> productPage = new PageImpl<>(Collections.singletonList(productDto));

        when(productService.findAll(pageable)).thenReturn(productPage);

        Page<ProductDto> result = productController.getProducts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getTitle());
        verify(productService, times(1)).findAll(pageable);
    }

    @Test
    void search_ShouldReturnPagedProducts_WhenCategoryIdIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        String categoryId = "cat123";
        ProductDto productDto = new ProductDto();
        productDto.setTitle("Search Result Product");
        Page<ProductDto> productPage = new PageImpl<>(Collections.singletonList(productDto));

        when(productService.search(categoryId, pageable)).thenReturn(productPage);

        Page<ProductDto> result = productController.search(categoryId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Search Result Product", result.getContent().get(0).getTitle());
        verify(productService, times(1)).search(categoryId, pageable);
    }
}
