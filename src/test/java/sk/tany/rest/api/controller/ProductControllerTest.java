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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.service.client.ProductClientService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Mock
    private ProductClientService productService;

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
    void getProduct_ShouldReturnProduct_WhenFound() {
        String productId = "prod123";
        ProductDto productDto = new ProductDto();
        productDto.setTitle("Found Product");

        when(productService.findById(productId)).thenReturn(Optional.of(productDto));

        ResponseEntity<ProductDto> result = productController.getProduct(productId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Found Product", result.getBody().getTitle());
        verify(productService, times(1)).findById(productId);
    }

    @Test
    void getProduct_ShouldReturnNotFound_WhenNotFound() {
        String productId = "prod123";

        when(productService.findById(productId)).thenReturn(Optional.empty());

        ResponseEntity<ProductDto> result = productController.getProduct(productId);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(productService, times(1)).findById(productId);
    }

    @Test
    void getProductsByCategory_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        String categoryId = "cat123";
        ProductDto productDto = new ProductDto();
        productDto.setTitle("Category Product");
        Page<ProductDto> productPage = new PageImpl<>(Collections.singletonList(productDto));

        when(productService.search(categoryId, pageable)).thenReturn(productPage);

        Page<ProductDto> result = productController.getProductsByCategory(categoryId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Category Product", result.getContent().get(0).getTitle());
        verify(productService, times(1)).search(categoryId, pageable);
    }
}
