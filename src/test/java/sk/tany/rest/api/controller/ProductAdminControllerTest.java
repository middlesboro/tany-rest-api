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
import sk.tany.rest.api.controller.admin.ProductAdminController;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.admin.product.list.ProductListResponse;
import sk.tany.rest.api.dto.admin.product.search.ProductSearchResponse;
import sk.tany.rest.api.mapper.ProductAdminApiMapper;
import sk.tany.rest.api.dto.admin.product.upload.ProductUploadImageResponse;
import sk.tany.rest.api.service.admin.ProductAdminService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductAdminControllerTest {

    @Mock
    private ProductAdminService productService;

    @Mock
    private ImageService imageService;

    @Mock
    private ProductAdminApiMapper productAdminApiMapper;

    @InjectMocks
    private ProductAdminController productAdminController;

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

        ProductListResponse response = new ProductListResponse();
        response.setTitle("Test Product");

        when(productService.findAll(any(ProductFilter.class), eq(pageable))).thenReturn(productPage);
        when(productAdminApiMapper.toListResponse(productDto)).thenReturn(response);

        Page<ProductListResponse> result = productAdminController.getProducts(new ProductFilter(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getTitle());
        verify(productService, times(1)).findAll(any(ProductFilter.class), eq(pageable));
    }

    @Test
    void search_ShouldReturnPagedProducts_WhenCategoryIdIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        String categoryId = "cat123";
        ProductDto productDto = new ProductDto();
        productDto.setTitle("Search Result Product");
        Page<ProductDto> productPage = new PageImpl<>(Collections.singletonList(productDto));

        ProductSearchResponse response = new ProductSearchResponse();
        response.setTitle("Search Result Product");

        when(productService.search(categoryId, pageable)).thenReturn(productPage);
        when(productAdminApiMapper.toSearchResponse(productDto)).thenReturn(response);

        Page<ProductSearchResponse> result = productAdminController.search(categoryId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Search Result Product", result.getContent().get(0).getTitle());
        verify(productService, times(1)).search(categoryId, pageable);
    }

    @Test
    void uploadImages_ShouldReturnUpdatedProduct() {
        String id = "1";
        ProductDto productDto = new ProductDto();
        productDto.setId(id);
        productDto.setImages(new ArrayList<>());

        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        org.springframework.web.multipart.MultipartFile[] files = {file};
        String imageUrl = "http://image.url";

        ProductUploadImageResponse response = new ProductUploadImageResponse();

        when(productService.findById(id)).thenReturn(Optional.of(productDto));
        when(imageService.upload(eq(file), eq(ImageKitType.PRODUCT))).thenReturn(imageUrl);
        when(productService.update(eq(id), any(ProductDto.class))).thenReturn(productDto);
        when(productAdminApiMapper.toUploadImageResponse(productDto)).thenReturn(response);

        org.springframework.http.ResponseEntity<ProductUploadImageResponse> result = productAdminController.uploadImages(id, files);

        assertEquals(org.springframework.http.HttpStatus.OK, result.getStatusCode());
        verify(imageService, times(1)).upload(file, ImageKitType.PRODUCT);
    }

    @Test
    void deleteImage_ShouldRemoveImageAndReturnNoContent() {
        String id = "1";
        String imageUrl = "http://image.url/to/delete";
        ProductDto productDto = new ProductDto();
        productDto.setId(id);
        List<String> images = new ArrayList<>();
        images.add(imageUrl);
        productDto.setImages(images);

        when(productService.findById(id)).thenReturn(Optional.of(productDto));
        when(productService.update(eq(id), any(ProductDto.class))).thenReturn(productDto);

        org.springframework.http.ResponseEntity<Void> result = productAdminController.deleteImage(id, imageUrl);

        assertEquals(org.springframework.http.HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(imageService, times(1)).delete(imageUrl);
        verify(productService, times(1)).update(eq(id), any(ProductDto.class));
        assertEquals(0, productDto.getImages().size());
    }
}
