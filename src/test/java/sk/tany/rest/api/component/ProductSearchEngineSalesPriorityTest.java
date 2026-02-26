package sk.tany.rest.api.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;
import sk.tany.rest.api.mapper.ProductLabelMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchEngineSalesPriorityTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private FilterParameterRepository filterParameterRepository;
    @Mock
    private FilterParameterValueRepository filterParameterValueRepository;
    @Mock
    private ProductSalesRepository productSalesRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private sk.tany.rest.api.domain.brand.BrandRepository brandRepository;
    @Mock
    private sk.tany.rest.api.domain.productlabel.ProductLabelRepository productLabelRepository;
    @Mock
    private FilterParameterMapper filterParameterMapper;
    @Mock
    private FilterParameterValueMapper filterParameterValueMapper;
    @Mock
    private ProductLabelMapper productLabelMapper;

    @InjectMocks
    private ProductSearchEngine productSearchEngine;

    @Test
    void searchAndSort_ShouldPrioritizeHigherSales_WhenRelevanceIsEqual() {
        // Arrange
        Product p1 = new Product();
        p1.setId("p1");
        p1.setTitle("Test Product");

        Product p2 = new Product();
        p2.setId("p2");
        p2.setTitle("Test Product");

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // Mock empty repositories for unused data
        when(filterParameterRepository.findAll()).thenReturn(Collections.emptyList());
        when(filterParameterValueRepository.findAll()).thenReturn(Collections.emptyList());
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(brandRepository.findAll()).thenReturn(Collections.emptyList());
        when(productLabelRepository.findAll()).thenReturn(Collections.emptyList());

        // Setup sales: p2 has more sales than p1
        ProductSales s1 = new ProductSales();
        s1.setProductId("p1");
        s1.setSalesCount(10);

        ProductSales s2 = new ProductSales();
        s2.setProductId("p2");
        s2.setSalesCount(100);

        when(productSalesRepository.findAll()).thenReturn(List.of(s1, s2));

        // Act
        productSearchEngine.loadProducts();
        List<Product> result = productSearchEngine.searchAndSort("Test Product");

        // Assert
        assertEquals(2, result.size());
        assertEquals("p2", result.get(0).getId(), "Product with higher sales should come first");
        assertEquals("p1", result.get(1).getId());
    }

    @Test
    void searchAndSort_ShouldHandleProductWithoutSalesRecord() {
        // Arrange
        Product p1 = new Product();
        p1.setId("p1");
        p1.setTitle("Test Product");
        // p1 has NO sales record (implicitly 0)

        Product p2 = new Product();
        p2.setId("p2");
        p2.setTitle("Test Product");
        // p2 has sales record (10)

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // Mock empty repositories for unused data
        when(filterParameterRepository.findAll()).thenReturn(Collections.emptyList());
        when(filterParameterValueRepository.findAll()).thenReturn(Collections.emptyList());
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(brandRepository.findAll()).thenReturn(Collections.emptyList());
        when(productLabelRepository.findAll()).thenReturn(Collections.emptyList());

        ProductSales s2 = new ProductSales();
        s2.setProductId("p2");
        s2.setSalesCount(10);

        // Only return s2
        when(productSalesRepository.findAll()).thenReturn(List.of(s2));

        // Act
        productSearchEngine.loadProducts();
        List<Product> result = productSearchEngine.searchAndSort("Test Product");

        // Assert
        assertEquals(2, result.size());
        assertEquals("p2", result.get(0).getId(), "Product with sales should come first");
        assertEquals("p1", result.get(1).getId());
    }
}
