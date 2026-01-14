package sk.tany.rest.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductFilterParameter;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.FilterParameterRequest;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchEngineTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private FilterParameterRepository filterParameterRepository;
    @Mock
    private FilterParameterValueRepository filterParameterValueRepository;
    @Mock
    private FilterParameterMapper filterParameterMapper;
    @Mock
    private FilterParameterValueMapper filterParameterValueMapper;

    @InjectMocks
    private ProductSearchEngine productSearchEngine;

    private Product product1;
    private Product product2;
    private FilterParameter colorParam;
    private FilterParameter brandParam;
    private FilterParameterValue redValue;
    private FilterParameterValue greenValue;
    private FilterParameterValue nikeValue;
    private FilterParameterValue adidasValue;

    @BeforeEach
    void setUp() {
        colorParam = new FilterParameter();
        colorParam.setId("color");
        colorParam.setName("Color");

        brandParam = new FilterParameter();
        brandParam.setId("brand");
        brandParam.setName("Brand");

        redValue = new FilterParameterValue();
        redValue.setId("red");
        redValue.setFilterParameterId("color");
        redValue.setName("Red");

        greenValue = new FilterParameterValue();
        greenValue.setId("green");
        greenValue.setFilterParameterId("color");
        greenValue.setName("Green");

        nikeValue = new FilterParameterValue();
        nikeValue.setId("nike");
        nikeValue.setFilterParameterId("brand");
        nikeValue.setName("Nike");

        adidasValue = new FilterParameterValue();
        adidasValue.setId("adidas");
        adidasValue.setFilterParameterId("brand");
        adidasValue.setName("Adidas");

        product1 = new Product();
        product1.setId("p1");
        product1.setTitle("Nike Red Shoe");
        product1.setCategoryIds(List.of("cat1"));
        product1.setProductFilterParameters(new ArrayList<>());

        ProductFilterParameter p1Color = new ProductFilterParameter();
        p1Color.setFilterParameterId("color");
        p1Color.setFilterParameterValueId("red");
        product1.getProductFilterParameters().add(p1Color);

        ProductFilterParameter p1Brand = new ProductFilterParameter();
        p1Brand.setFilterParameterId("brand");
        p1Brand.setFilterParameterValueId("nike");
        product1.getProductFilterParameters().add(p1Brand);

        product2 = new Product();
        product2.setId("p2");
        product2.setTitle("Adidas Green Shoe");
        product2.setCategoryIds(List.of("cat1"));
        product2.setProductFilterParameters(new ArrayList<>());

        ProductFilterParameter p2Color = new ProductFilterParameter();
        p2Color.setFilterParameterId("color");
        p2Color.setFilterParameterValueId("green");
        product2.getProductFilterParameters().add(p2Color);

        ProductFilterParameter p2Brand = new ProductFilterParameter();
        p2Brand.setFilterParameterId("brand");
        p2Brand.setFilterParameterValueId("adidas");
        product2.getProductFilterParameters().add(p2Brand);

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));
        when(filterParameterRepository.findAll()).thenReturn(List.of(colorParam, brandParam));
        when(filterParameterValueRepository.findAll()).thenReturn(List.of(redValue, greenValue, nikeValue, adidasValue));

        // Mock mappers leniently because some tests might not trigger them
        // In Mockito 3+, unnecessary stubs throw exceptions. Using lenient() avoids this.
        // Actually, let's just make the when calls, if they are unused they are unused.
        // Wait, UnnecessaryStubbingException IS the default strictness.

    }

    private void setupMappers() {
         when(filterParameterMapper.toDto(any(FilterParameter.class))).thenAnswer(invocation -> {
            FilterParameter p = invocation.getArgument(0);
            FilterParameterDto dto = new FilterParameterDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            return dto;
        });

        when(filterParameterValueMapper.toDto(any(FilterParameterValue.class))).thenAnswer(invocation -> {
            FilterParameterValue v = invocation.getArgument(0);
            FilterParameterValueDto dto = new FilterParameterValueDto();
            dto.setId(v.getId());
            dto.setName(v.getName());
            return dto;
        });
    }

    @Test
    void searchAndSort_ShouldReturnRelevantProducts() {
        productSearchEngine.loadProducts();

        List<Product> result = productSearchEngine.searchAndSort("Nike");

        assertEquals(1, result.size());
        assertEquals("Nike Red Shoe", result.get(0).getTitle());
    }

    @Test
    void searchAndSort_ShouldHandleTypo() {
        productSearchEngine.loadProducts();

        // "Niek" instead of "Nike" (Levenshtein distance 2 should match)
        List<Product> result = productSearchEngine.searchAndSort("Niek");

        assertEquals(1, result.size());
        assertEquals("Nike Red Shoe", result.get(0).getTitle());
    }

    @Test
    void getFilterParametersForCategory_ShouldReturnAllParams() {
        setupMappers();
        productSearchEngine.loadProducts();

        List<FilterParameterDto> result = productSearchEngine.getFilterParametersForCategory("cat1");

        assertEquals(2, result.size());
    }

    @Test
    void getFilterParametersForCategoryWithFilter_ShouldReturnFiltersWithSelectedState() {
        setupMappers();
        productSearchEngine.loadProducts();

        CategoryFilterRequest request = new CategoryFilterRequest();
        FilterParameterRequest brandRequest = new FilterParameterRequest();
        brandRequest.setId("brand");
        brandRequest.setFilterParameterValueIds(List.of("nike"));
        request.setFilterParameters(List.of(brandRequest));

        // Modified Logic Plan: Return ALL category filters, mark selected.
        // If I haven't implemented that change yet, this test will fail if I expect "Adidas" to be present (it was filtered out in previous impl).
        // I will update the impl in the next step. So here I write the test for the NEW behavior.
        // Expectation: Brand filter contains BOTH Nike and Adidas. Nike is selected.

        List<FilterParameterDto> result = productSearchEngine.getFilterParametersForCategoryWithFilter("cat1", request);

        assertEquals(2, result.size());

        FilterParameterDto brandDto = result.stream().filter(f -> f.getId().equals("brand")).findFirst().orElseThrow();
        assertEquals(2, brandDto.getValues().size()); // Should have both

        FilterParameterValueDto nikeDto = brandDto.getValues().stream().filter(v -> v.getId().equals("nike")).findFirst().orElseThrow();
        assertTrue(nikeDto.getSelected());

        FilterParameterValueDto adidasDto = brandDto.getValues().stream().filter(v -> v.getId().equals("adidas")).findFirst().orElseThrow();
        assertFalse(adidasDto.getSelected());
    }
}
