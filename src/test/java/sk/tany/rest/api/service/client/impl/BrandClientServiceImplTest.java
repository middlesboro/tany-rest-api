package sk.tany.rest.api.service.client.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.mapper.BrandMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandClientServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductSearchEngine productSearchEngine;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandClientServiceImpl brandClientService;

    @Test
    void findAll_returnsOnlyBrandsWithActiveProducts() {
        Brand brand1 = new Brand();
        brand1.setId("id1");
        brand1.setName("Brand 1");

        Brand brand2 = new Brand();
        brand2.setId("id2");
        brand2.setName("Brand 2");

        BrandDto brandDto1 = new BrandDto();
        brandDto1.setId("id1");

        when(brandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(Arrays.asList(brand1, brand2));
        when(productSearchEngine.hasActiveProductWithBrand("id1")).thenReturn(true);
        when(productSearchEngine.hasActiveProductWithBrand("id2")).thenReturn(false);
        when(brandMapper.toDto(brand1)).thenReturn(brandDto1);

        List<BrandDto> result = brandClientService.findAll();

        assertEquals(1, result.size());
        assertEquals("id1", result.get(0).getId());

        verify(brandRepository).findAll(Sort.by(Sort.Direction.ASC, "name"));
        verify(productSearchEngine).hasActiveProductWithBrand("id1");
        verify(productSearchEngine).hasActiveProductWithBrand("id2");
        verify(brandMapper).toDto(brand1);
        verify(brandMapper, never()).toDto(brand2);
    }
}
