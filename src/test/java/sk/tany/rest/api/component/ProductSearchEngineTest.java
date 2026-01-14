package sk.tany.rest.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    private ProductSearchEngine productSearchEngine;

    @BeforeEach
    void setUp() {
        productSearchEngine = new ProductSearchEngine(
            productRepository,
            filterParameterRepository,
            filterParameterValueRepository,
            filterParameterMapper,
            filterParameterValueMapper
        );
    }

    @Test
    void shouldFindAndSortProductsByRelevance() {
        Product p1 = new Product();
        p1.setTitle("Samsung Galaxy S21");
        Product p2 = new Product();
        p2.setTitle("Samsung Galaxy Charger");
        Product p3 = new Product();
        p3.setTitle("Iphone 13");
        Product p4 = new Product();
        p4.setTitle("Samsung Galaxy Case");

        when(productRepository.findAll()).thenReturn(Arrays.asList(p1, p2, p3, p4));

        // Load data manually since @EventListener won't fire in unit test without Spring context
        productSearchEngine.loadProducts();

        List<Product> results = productSearchEngine.searchAndSort("samsung galaxy");

        assertThat(results).contains(p1, p2, p4);
        assertThat(results).doesNotContain(p3);
        assertThat(results.get(0)).isEqualTo(p1); // Most relevant
    }

    @Test
    void shouldHandleTypos() {
        Product p1 = new Product();
        p1.setTitle("Bluetooth Speaker");

        when(productRepository.findAll()).thenReturn(List.of(p1));
        productSearchEngine.loadProducts();

        // "Blutooth" is a typo
        List<Product> results = productSearchEngine.searchAndSort("Blutooth");

        assertThat(results).contains(p1);
    }

    @Test
    void shouldHandleDiacritics() {
        Product p1 = new Product();
        p1.setTitle("Špeciálny produkt");

        when(productRepository.findAll()).thenReturn(List.of(p1));
        productSearchEngine.loadProducts();

        List<Product> results = productSearchEngine.searchAndSort("specialny");

        assertThat(results).contains(p1);
    }

    @Test
    void shouldReturnEmptyListForEmptyQuery() {
        List<Product> results = productSearchEngine.searchAndSort("");
        assertThat(results).isEmpty();
    }

    @Test
    void shouldAddProduct() {
        Product p1 = new Product();
        p1.setId("1");
        p1.setTitle("New Product");

        productSearchEngine.addProduct(p1);

        List<Product> results = productSearchEngine.searchAndSort("new product");
        assertThat(results).contains(p1);
    }

    @Test
    void shouldUpdateProduct() {
        Product p1 = new Product();
        p1.setId("1");
        p1.setTitle("Old Product");

        productSearchEngine.addProduct(p1);

        Product p2 = new Product();
        p2.setId("1");
        p2.setTitle("Updated Product");

        productSearchEngine.updateProduct(p2);

        List<Product> results = productSearchEngine.searchAndSort("Updated");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Updated Product");
    }

    @Test
    void shouldRemoveProduct() {
        Product p1 = new Product();
        p1.setId("1");
        p1.setTitle("Delete Me");

        productSearchEngine.addProduct(p1);
        productSearchEngine.removeProduct("1");

        List<Product> results = productSearchEngine.searchAndSort("delete me");
        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnFilterParametersForCategory() {
        String categoryId = "cat1";

        Product p1 = new Product();
        p1.setId("p1");
        p1.setCategoryIds(List.of(categoryId));
        ProductFilterParameter pfp1 = new ProductFilterParameter();
        pfp1.setFilterParameterId("fp1");
        pfp1.setFilterParameterValueId("fpv1");
        p1.setProductFilterParameters(List.of(pfp1));

        FilterParameter fp1 = new FilterParameter();
        fp1.setId("fp1");
        fp1.setName("Color");

        FilterParameterValue fpv1 = new FilterParameterValue();
        fpv1.setId("fpv1");
        fpv1.setName("Red");

        FilterParameterDto fpDto = new FilterParameterDto();
        fpDto.setId("fp1");
        fpDto.setName("Color");

        FilterParameterValueDto fpvDto = new FilterParameterValueDto();
        fpvDto.setId("fpv1");
        fpvDto.setName("Red");

        when(productRepository.findAll()).thenReturn(List.of(p1));
        when(filterParameterRepository.findAll()).thenReturn(List.of(fp1));
        when(filterParameterValueRepository.findAll()).thenReturn(List.of(fpv1));

        when(filterParameterMapper.toDto(fp1)).thenReturn(fpDto);
        when(filterParameterValueMapper.toDto(fpv1)).thenReturn(fpvDto);

        productSearchEngine.loadProducts();

        List<FilterParameterDto> results = productSearchEngine.getFilterParametersForCategory(categoryId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo("fp1");
        assertThat(results.get(0).getValues()).hasSize(1);
        assertThat(results.get(0).getValues().get(0).getId()).isEqualTo("fpv1");
    }
}
