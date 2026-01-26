package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.common.ImageService;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDiscountTest {

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

    @InjectMocks
    private ProductAdminServiceImpl productAdminService;

    @Test
    void shouldCalculateDiscountPriceWithoutVat() {
        ProductAdminDto dto = new ProductAdminDto();
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(120)); // Price with VAT
        product.setPriceWithoutVat(BigDecimal.valueOf(100)); // Price without VAT (20% VAT)
        product.setDiscountPrice(BigDecimal.valueOf(60)); // Discounted Price with VAT (50% off)

        // Expected Discount Price Without VAT: 60 * (100/120) = 50

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.save(dto);

        assertThat(product.getDiscountPriceWithoutVat()).isNotNull();
        assertThat(product.getDiscountPriceWithoutVat()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    void shouldHandleNullDiscountPrice() {
        ProductAdminDto dto = new ProductAdminDto();
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(120));
        product.setPriceWithoutVat(BigDecimal.valueOf(100));
        product.setDiscountPrice(null);

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.save(dto);

        assertThat(product.getDiscountPriceWithoutVat()).isNull();
    }

    @Test
    void shouldHandleZeroPrice() {
        ProductAdminDto dto = new ProductAdminDto();
        Product product = new Product();
        product.setPrice(BigDecimal.ZERO);
        product.setPriceWithoutVat(BigDecimal.ZERO);
        product.setDiscountPrice(BigDecimal.valueOf(10));

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toAdminDto(any(Product.class))).thenReturn(dto);

        productAdminService.save(dto);

        assertThat(product.getDiscountPriceWithoutVat()).isNull();
    }
}
