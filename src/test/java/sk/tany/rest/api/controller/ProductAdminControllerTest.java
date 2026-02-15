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
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateRequest;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateResponse;
import sk.tany.rest.api.dto.admin.product.search.ProductSearchResponse;
import sk.tany.rest.api.dto.admin.product.upload.ProductUploadImageResponse;
import sk.tany.rest.api.mapper.ProductAdminApiMapper;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.admin.ProductAdminService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;
import sk.tany.rest.api.service.scheduler.InvoiceUploadScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductAdminControllerTest {

    @Mock
    private ProductAdminService productService;

    @Mock
    private ImageService imageService;

    @Mock
    private ProductAdminApiMapper productAdminApiMapper;

    @Mock
    private PrestaShopImportService prestaShopImportService;

    @Mock
    private InvoiceUploadScheduler invoiceUploadScheduler;

    @InjectMocks
    private ProductAdminController productAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void search_ShouldReturnPagedProducts_WhenCategoryIdIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        String categoryId = "cat123";
        ProductAdminDto productDto = new ProductAdminDto();
        productDto.setTitle("Search Result Product");
        Page<ProductAdminDto> productPage = new PageImpl<>(Collections.singletonList(productDto));

        ProductSearchResponse response = new ProductSearchResponse();
        response.setTitle("Search Result Product");

        when(productService.search(categoryId, pageable)).thenReturn(productPage);
        when(productAdminApiMapper.toSearchResponse(productDto)).thenReturn(response);

        Page<ProductSearchResponse> result = productAdminController.search(categoryId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Search Result Product", result.getContent().getFirst().getTitle());
        verify(productService, times(1)).search(categoryId, pageable);
    }

    @Test
    void createProduct_shouldPassProductIdentifier() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setTitle("New Product");
        request.setProductIdentifier(999L);

        ProductAdminDto dto = new ProductAdminDto();
        dto.setTitle("New Product");
        dto.setProductIdentifier(999L);

        ProductAdminDto savedDto = new ProductAdminDto();
        savedDto.setId("1");
        savedDto.setTitle("New Product");
        savedDto.setProductIdentifier(999L);

        ProductCreateResponse response = new ProductCreateResponse();
        response.setId("1");
        response.setTitle("New Product");
        response.setProductIdentifier(999L);

        when(productAdminApiMapper.toDto(request)).thenReturn(dto);
        when(productService.save(dto)).thenReturn(savedDto);
        when(productAdminApiMapper.toCreateResponse(savedDto)).thenReturn(response);

        org.springframework.http.ResponseEntity<ProductCreateResponse> result = productAdminController.createProduct(request);

        assertThat(result.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProductIdentifier()).isEqualTo(999L);

        verify(productService).save(dto);
        verify(productAdminApiMapper).toDto(request);
        verify(productAdminApiMapper).toCreateResponse(savedDto);
    }

    @Test
    void uploadImages_ShouldReturnUpdatedProduct() {
        String id = "1";
        ProductAdminDto productDto = new ProductAdminDto();
        productDto.setId(id);
        productDto.setImages(new ArrayList<>());

        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        org.springframework.web.multipart.MultipartFile[] files = {file};
        String imageUrl = "http://image.url";

        ProductUploadImageResponse response = new ProductUploadImageResponse();

        when(productService.findById(id)).thenReturn(Optional.of(productDto));
        when(imageService.upload(eq(file), eq(ImageKitType.PRODUCT))).thenReturn(imageUrl);
        when(productService.update(eq(id), any(ProductAdminDto.class))).thenReturn(productDto);
        when(productAdminApiMapper.toUploadImageResponse(productDto)).thenReturn(response);

        org.springframework.http.ResponseEntity<ProductUploadImageResponse> result = productAdminController.uploadImages(id, files);

        assertEquals(org.springframework.http.HttpStatus.OK, result.getStatusCode());
        verify(imageService, times(1)).upload(file, ImageKitType.PRODUCT);
    }

    @Test
    void deleteImage_ShouldRemoveImageAndReturnNoContent() {
        String id = "1";
        String imageUrl = "http://image.url/to/delete";
        ProductAdminDto productDto = new ProductAdminDto();
        productDto.setId(id);
        List<String> images = new ArrayList<>();
        images.add(imageUrl);
        productDto.setImages(images);

        when(productService.findById(id)).thenReturn(Optional.of(productDto));
        when(productService.update(eq(id), any(ProductAdminDto.class))).thenReturn(productDto);

        org.springframework.http.ResponseEntity<Void> result = productAdminController.deleteImage(id, imageUrl);

        assertEquals(org.springframework.http.HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(imageService, times(1)).delete(imageUrl);
        verify(productService, times(1)).update(eq(id), any(ProductAdminDto.class));
        assertEquals(0, productDto.getImages().size());
    }
}
