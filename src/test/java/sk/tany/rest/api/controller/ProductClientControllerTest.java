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
import sk.tany.rest.api.controller.client.ProductClientController;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.client.product.get.ProductClientGetResponse;
import sk.tany.rest.api.dto.client.product.list.ProductClientListResponse;
import sk.tany.rest.api.mapper.ProductClientApiMapper;
import sk.tany.rest.api.service.client.ProductClientService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ProductClientControllerTest {

    @Mock
    private ProductClientService productService;

    @Mock
    private ProductClientApiMapper productClientApiMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductClientController productClientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProducts_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductClientDto productDto = new ProductClientDto();
        productDto.setTitle("Test Product");
        Page<ProductClientDto> productPage = new PageImpl<>(Collections.singletonList(productDto));

        ProductClientListResponse response = new ProductClientListResponse();
        response.setTitle("Test Product");

        when(productService.findAll(pageable)).thenReturn(productPage);
        when(productClientApiMapper.toListResponse(productDto)).thenReturn(response);

        Page<ProductClientListResponse> result = productClientController.getProducts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().getFirst().getTitle());
        verify(productService, times(1)).findAll(pageable);
    }

    @Test
    void getProduct_ShouldReturnProduct_WhenFound() {
        String productId = "prod123";
        ProductClientDto productDto = new ProductClientDto();
        productDto.setTitle("Found Product");

        ProductClientGetResponse response = new ProductClientGetResponse();
        response.setTitle("Found Product");

        when(productService.findById(productId)).thenReturn(Optional.of(productDto));
        when(productClientApiMapper.toGetResponse(productDto)).thenReturn(response);

        ResponseEntity<ProductClientGetResponse> result = productClientController.getProduct(productId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Found Product", result.getBody().getTitle());
        verify(productService, times(1)).findById(productId);
    }

    @Test
    void getProduct_ShouldReturnNotFound_WhenNotFound() {
        String productId = "prod123";

        when(productService.findById(productId)).thenReturn(Optional.empty());

        ResponseEntity<ProductClientGetResponse> result = productClientController.getProduct(productId);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(productService, times(1)).findById(productId);
    }

    @Test
    void getProductBySlug_WhenFound_AndDefaultCategoryExists() {
        String slug = "some-product";
        String categoryId = "cat1";
        String defaultCategoryTitle = "Default Category";

        ProductClientDto dto = new ProductClientDto();
        dto.setCategoryIds(Collections.singletonList(categoryId));
        dto.setDefaultCategoryId(categoryId);

        ProductClientGetResponse responseDto = new ProductClientGetResponse();
        responseDto.setCategoryIds(Collections.singletonList(categoryId));
        responseDto.setDefaultCategoryId(categoryId);

        Category category = new Category();
        category.setId(categoryId);
        category.setTitle(defaultCategoryTitle);
        category.setDefaultCategory(true);

        when(productService.findBySlug(slug)).thenReturn(Optional.of(dto));
        when(productClientApiMapper.toGetResponse(dto)).thenReturn(responseDto);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        ResponseEntity<ProductClientGetResponse> response = productClientController.getProductBySlug(slug);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(defaultCategoryTitle, response.getBody().getDefaultCategoryTitle());
    }

    @Test
    void getProductBySlug_WhenFound_AndNoDefaultCategory() {
        String slug = "some-product";
        String categoryId = "cat1";

        ProductClientDto dto = new ProductClientDto();
        dto.setCategoryIds(Collections.singletonList(categoryId));

        ProductClientGetResponse responseDto = new ProductClientGetResponse();
        responseDto.setCategoryIds(Collections.singletonList(categoryId));

        when(productService.findBySlug(slug)).thenReturn(Optional.of(dto));
        when(productClientApiMapper.toGetResponse(dto)).thenReturn(responseDto);

        ResponseEntity<ProductClientGetResponse> response = productClientController.getProductBySlug(slug);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getDefaultCategoryTitle());
    }

    @Test
    void getProductBySlug_WhenNotFound() {
        String slug = "unknown";
        when(productService.findBySlug(slug)).thenReturn(Optional.empty());

        ResponseEntity<ProductClientGetResponse> response = productClientController.getProductBySlug(slug);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProductsByCategory_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        String categoryId = "cat123";
        ProductClientDto productDto = new ProductClientDto();
        productDto.setTitle("Category Product");
        Page<ProductClientDto> productPage = new PageImpl<>(Collections.singletonList(productDto));

        ProductClientListResponse response = new ProductClientListResponse();
        response.setTitle("Category Product");

        when(productService.search(categoryId, pageable)).thenReturn(productPage);
        when(productClientApiMapper.toListResponse(productDto)).thenReturn(response);

        Page<ProductClientListResponse> result = productClientController.getProductsByCategory(categoryId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Category Product", result.getContent().getFirst().getTitle());
        verify(productService, times(1)).search(categoryId, pageable);
    }

    @Test
    void searchProducts_ShouldReturnList() {
        String query = "search query";
        ProductClientDto productDto = new ProductClientDto();
        productDto.setTitle("Searched Product");

        ProductClientListResponse response = new ProductClientListResponse();
        response.setTitle("Searched Product");

        when(productService.searchProducts(query)).thenReturn(java.util.List.of(productDto));
        when(productClientApiMapper.toListResponse(productDto)).thenReturn(response);

        java.util.List<ProductClientListResponse> result = productClientController.searchProducts(query);

        assertEquals(1, result.size());
        assertEquals("Searched Product", result.getFirst().getTitle());
        verify(productService, times(1)).searchProducts(query);
    }
}
