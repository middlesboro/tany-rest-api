package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.controller.admin.ProductAdminController;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.service.ImageService;
import sk.tany.rest.api.service.ProductService;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductImageUploadTest {

    @Mock
    private ProductService productService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ProductAdminController productAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadImages_ShouldAddImagesToProductAndReturnUpdatedProduct() {
        // Arrange
        String productId = "123";
        MockMultipartFile file1 = new MockMultipartFile("files", "image1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "image2.jpg", "image/jpeg", "content2".getBytes());
        MultipartFile[] files = {file1, file2};

        ProductDto existingProduct = new ProductDto();
        existingProduct.setId(productId);
        existingProduct.setImages(new ArrayList<>());

        when(productService.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(imageService.upload(file1)).thenReturn("http://imagekit.io/image1.jpg");
        when(imageService.upload(file2)).thenReturn("http://imagekit.io/image2.jpg");

        when(productService.update(eq(productId), any(ProductDto.class))).thenAnswer(invocation -> invocation.getArgument(1));

        // Act
        ResponseEntity<ProductDto> response = productAdminController.uploadImages(productId, files);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getImages().size());
        assertEquals("http://imagekit.io/image1.jpg", response.getBody().getImages().get(0));
        assertEquals("http://imagekit.io/image2.jpg", response.getBody().getImages().get(1));

        verify(productService).findById(productId);
        verify(imageService, times(2)).upload(any(MultipartFile.class));
        verify(productService).update(eq(productId), any(ProductDto.class));
    }

    @Test
    void uploadImages_ShouldReturnNotFound_WhenProductDoesNotExist() {
        // Arrange
        String productId = "non-existent";
        MockMultipartFile file = new MockMultipartFile("files", "image.jpg", "image/jpeg", "content".getBytes());
        MultipartFile[] files = {file};

        when(productService.findById(productId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ProductDto> response = productAdminController.uploadImages(productId, files);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        verify(productService).findById(productId);
        verify(imageService, never()).upload(any());
        verify(productService, never()).update(anyString(), any());
    }
}
