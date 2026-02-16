package sk.tany.rest.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;
import sk.tany.rest.api.mapper.ProductLabelMapper;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.SortOption;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchEngineStockTest {

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

    private Product productInStock1;
    private Product productInStock2;
    private Product productOutOfStock;
    private Product productNullStock;

    @BeforeEach
    void setUp() {
        productInStock1 = new Product();
        productInStock1.setId("p1");
        productInStock1.setTitle("Alpha In Stock");
        productInStock1.setQuantity(10);
        productInStock1.setCategoryIds(List.of("cat1"));

        productInStock2 = new Product();
        productInStock2.setId("p2");
        productInStock2.setTitle("Beta In Stock");
        productInStock2.setQuantity(5);
        productInStock2.setCategoryIds(List.of("cat1"));

        productOutOfStock = new Product();
        productOutOfStock.setId("p3");
        productOutOfStock.setTitle("Gamma Out Stock");
        productOutOfStock.setQuantity(0);
        productOutOfStock.setCategoryIds(List.of("cat1"));

        productNullStock = new Product();
        productNullStock.setId("p4");
        productNullStock.setTitle("Delta Null Stock");
        productNullStock.setQuantity(null);
        productNullStock.setCategoryIds(List.of("cat1"));

        when(productRepository.findAll()).thenReturn(List.of(productOutOfStock, productInStock1, productNullStock, productInStock2));
        when(filterParameterRepository.findAll()).thenReturn(Collections.emptyList());
        when(filterParameterValueRepository.findAll()).thenReturn(Collections.emptyList());
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(productSalesRepository.findAll()).thenReturn(Collections.emptyList());
        when(productLabelRepository.findAll()).thenReturn(Collections.emptyList());
        when(brandRepository.findAll()).thenReturn(Collections.emptyList());

        productSearchEngine.loadProducts();
    }

    @Test
    void findAll_ShouldPrioritizeStock_WhenRequested() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productSearchEngine.findAll(pageable, true);

        List<Product> content = result.getContent();
        assertEquals(4, content.size());

        // In stock should come first
        assertTrue(content.get(0).getQuantity() > 0);
        assertTrue(content.get(1).getQuantity() > 0);

        // Out of stock/Null stock should come last
        assertFalse(content.get(2).getQuantity() != null && content.get(2).getQuantity() > 0);
        assertFalse(content.get(3).getQuantity() != null && content.get(3).getQuantity() > 0);
    }

    @Test
    void findAll_ShouldNotPrioritizeStock_WhenNotRequested() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productSearchEngine.findAll(pageable, false);

        List<Product> content = result.getContent();
        assertEquals(4, content.size());

        // Order should be insertion order (as returned by repo)
        assertEquals("p3", content.get(0).getId()); // Out of stock
        assertEquals("p1", content.get(1).getId()); // In stock
        assertEquals("p4", content.get(2).getId()); // Null stock
        assertEquals("p2", content.get(3).getId()); // In stock
    }

    @Test
    void findByCategoryIds_ShouldPrioritizeStock_WhenRequested() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        Page<Product> result = productSearchEngine.findByCategoryIds("cat1", pageable, true);

        List<Product> content = result.getContent();
        assertEquals(4, content.size());

        // Expect: In Stock (Alpha, Beta) then Out/Null (Delta, Gamma) - assuming Delta comes before Gamma alphabetically
        assertEquals("Alpha In Stock", content.get(0).getTitle());
        assertEquals("Beta In Stock", content.get(1).getTitle());
        assertEquals("Delta Null Stock", content.get(2).getTitle());
        assertEquals("Gamma Out Stock", content.get(3).getTitle());
    }

    @Test
    void findByCategoryIds_ShouldNotPrioritizeStock_WhenNotRequested() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        Page<Product> result = productSearchEngine.findByCategoryIds("cat1", pageable, false);

        List<Product> content = result.getContent();
        assertEquals(4, content.size());

        // Expect: Alphabetical order regardless of stock
        assertEquals("Alpha In Stock", content.get(0).getTitle());
        assertEquals("Beta In Stock", content.get(1).getTitle());
        assertEquals("Delta Null Stock", content.get(2).getTitle());
        assertEquals("Gamma Out Stock", content.get(3).getTitle());
    }

    @Test
    void searchAndSort_ShouldPrioritizeStock_WhenRequested() {
        // Query "Stock" matches all
        List<Product> result = productSearchEngine.searchAndSort("Stock", true);

        assertEquals(4, result.size());

        // In stock first
        assertTrue(result.get(0).getQuantity() > 0);
        assertTrue(result.get(1).getQuantity() > 0);
    }

    @Test
    void search_ShouldAlwaysPrioritizeStock() {
        CategoryFilterRequest request = new CategoryFilterRequest();
        request.setSort(SortOption.NAME_ASC);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(4, result.size());

        // Expect: In Stock (Alpha, Beta) then Out/Null (Delta, Gamma)
        assertEquals("Alpha In Stock", result.get(0).getTitle());
        assertEquals("Beta In Stock", result.get(1).getTitle());
        assertEquals("Delta Null Stock", result.get(2).getTitle());
        assertEquals("Gamma Out Stock", result.get(3).getTitle());
    }
}
