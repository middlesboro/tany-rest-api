package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.domain.supplier.Supplier;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.dto.isklad.UpdateInventoryCardRequest;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.SequenceService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    @Mock
    private sk.tany.rest.api.component.SlugGenerator slugGenerator;
    @Mock
    private SequenceService sequenceService;
    @Mock
    private sk.tany.rest.api.service.isklad.ISkladService iskladService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ProductAdminServiceImpl productAdminService;

    @Test
    void save_shouldSetPrestashopId_whenMissing() {
        ProductAdminDto dto = new ProductAdminDto();
        Product product = new Product();
        when(productMapper.toEntity(dto)).thenReturn(product);
        when(sequenceService.getNextSequence("product_identifier")).thenReturn(100L);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.save(dto);

        assertThat(product.getProductIdentifier()).isEqualTo(100L);
        verify(sequenceService).getNextSequence("product_identifier");
    }

    @Test
    void save_shouldEnsureSequenceAtLeast_whenIdentifierProvided() {
        ProductAdminDto dto = new ProductAdminDto();
        dto.setProductIdentifier(500L);
        Product product = new Product();
        product.setProductIdentifier(500L);
        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.save(dto);

        verify(sequenceService).ensureSequenceAtLeast("product_identifier", 500L);
        verify(sequenceService, never()).getNextSequence(anyString());
    }

    @Test
    void recalculateReviewStatistics_shouldSetZero_whenNoReviews() {
        String productId = "p1";
        ProductAdminDto dto = new ProductAdminDto();
        dto.setId(productId);
        Product product = new Product();
        product.setId(productId);

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(reviewRepository.findAllByProductId(productId)).thenReturn(Collections.emptyList());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.update(productId, dto);

        assertThat(product.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(product.getReviewsCount()).isEqualTo(0);
    }

    @Test
    void recalculateReviewStatistics_shouldCalculateCorrectly_whenActiveReviewsExist() {
        String productId = "p1";
        ProductAdminDto dto = new ProductAdminDto();
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
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.update(productId, dto);

        assertThat(product.getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
        assertThat(product.getReviewsCount()).isEqualTo(2);
    }

    @Test
    void recalculateReviewStatistics_shouldIgnoreInactiveReviews() {
        String productId = "p1";
        ProductAdminDto dto = new ProductAdminDto();
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
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.update(productId, dto);

        assertThat(product.getAverageRating()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(product.getReviewsCount()).isEqualTo(1);
    }

    @Test
    void searchByQuery_shouldReturnMappedProducts() {
        String query = "test";
        Product product = new Product();
        product.setId("1");
        product.setTitle("Test Product");
        ProductAdminDto dto = new ProductAdminDto();
        dto.setId("1");
        dto.setTitle("Test Product");

        when(productSearchEngine.searchAndSort(query, false)).thenReturn(List.of(product));
        when(productMapper.toAdminDto(product)).thenReturn(dto);

        List<ProductAdminDto> result = productAdminService.searchByQuery(query);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Test Product");
        verify(productSearchEngine).searchAndSort(query, false);
    }

    @Test
    void findAllByFilterParameterValueId_shouldReturnMappedProducts() {
        String filterParameterValueId = "fpv1";
        Product product = new Product();
        product.setId("1");
        product.setTitle("Test Product");
        ProductAdminDto dto = new ProductAdminDto();
        dto.setId("1");
        dto.setTitle("Test Product");

        when(productRepository.findAllByProductFilterParametersFilterParameterValueId(filterParameterValueId))
                .thenReturn(List.of(product));
        when(productMapper.toAdminDto(product)).thenReturn(dto);

        List<ProductAdminDto> result = productAdminService.findAllByFilterParameterValueId(filterParameterValueId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Test Product");
        verify(productRepository).findAllByProductFilterParametersFilterParameterValueId(filterParameterValueId);
    }

    @Test
    void updateAllProductsQuantity_shouldUpdateQuantityForAllProducts() {
        int quantity = 10;
        Product p1 = new Product();
        p1.setId("1");
        Product p2 = new Product();
        p2.setId("2");
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        productAdminService.updateAllProductsQuantity(quantity);

        assertThat(p1.getQuantity()).isEqualTo(quantity);
        assertThat(p2.getQuantity()).isEqualTo(quantity);
        verify(productRepository, times(2)).save(any(Product.class));
        verify(productSearchEngine, times(2)).updateProduct(any(Product.class));
    }

    @Test
    void save_shouldSendCompleteProductDataToIsklad() {
        ProductAdminDto dto = new ProductAdminDto();
        Product product = new Product();
        product.setProductIdentifier(123L);
        product.setTitle("Test Product");
        product.setEan("123456789");
        product.setPriceWithoutVat(BigDecimal.TEN);
        product.setActive(true);
        product.setBrandId("brand1");
        product.setSupplierId("supplier1");
        product.setImages(List.of("img1.jpg", "img2.jpg"));

        Brand brand = new Brand();
        brand.setName("Test Brand");
        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier");

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);
        when(brandRepository.findById("brand1")).thenReturn(Optional.of(brand));
        when(supplierRepository.findById("supplier1")).thenReturn(Optional.of(supplier));

        productAdminService.save(dto);

        ArgumentCaptor<UpdateInventoryCardRequest> requestCaptor = ArgumentCaptor.forClass(UpdateInventoryCardRequest.class);
        verify(iskladService).createOrUpdateProduct(requestCaptor.capture());

        UpdateInventoryCardRequest request = requestCaptor.getValue();
        assertThat(request.getItemId()).isEqualTo(123L);
        assertThat(request.getName()).isEqualTo("Test Product");
        assertThat(request.getEan()).isEqualTo("123456789");
        assertThat(request.getPriceWithoutTax()).isEqualTo(BigDecimal.TEN);
        assertThat(request.getMj()).isEqualTo("ks");
        assertThat(request.getEnabled()).isTrue();
        assertThat(request.getProducer()).isEqualTo("Test Brand");
        assertThat(request.getSupplier()).isEqualTo("Test Supplier");
        assertThat(request.getImages()).containsExactly("img1.jpg");
    }
}
