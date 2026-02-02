package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.admin.brand.BrandAdminGetResponse;
import sk.tany.rest.api.mapper.BrandMapper;
import sk.tany.rest.api.service.common.ImageService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandAdminServiceImplTest {

    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BrandMapper brandMapper;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private BrandAdminServiceImpl brandAdminService;

    @Test
    void findDetailById_shouldReturnBrandWithProductIds() {
        String brandId = "b1";
        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName("Test Brand");

        Product p1 = new Product();
        p1.setId("p1");
        p1.setBrandId(brandId);
        Product p2 = new Product();
        p2.setId("p2");
        p2.setBrandId(brandId);

        BrandAdminGetResponse responseDto = new BrandAdminGetResponse();
        responseDto.setId(brandId);
        responseDto.setProductIds(List.of("p1", "p2"));

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandId(brandId)).thenReturn(List.of(p1, p2));
        when(brandMapper.toAdminDetailDto(brand, List.of("p1", "p2"))).thenReturn(responseDto);

        Optional<BrandAdminGetResponse> result = brandAdminService.findDetailById(brandId);

        assertThat(result).isPresent();
        assertThat(result.get().getProductIds()).containsExactly("p1", "p2");
        verify(brandRepository).findById(brandId);
        verify(productRepository).findAllByBrandId(brandId);
    }
}
