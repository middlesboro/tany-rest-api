package sk.tany.rest.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductFilterParameter;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.FilterParameterRequest;
import sk.tany.rest.api.dto.request.SortOption;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
    private ProductSalesRepository productSalesRepository;
    @Mock
    private FilterParameterMapper filterParameterMapper;
    @Mock
    private FilterParameterValueMapper filterParameterValueMapper;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductSearchEngine productSearchEngine;

    private Category category1;
    private Category category2;

    private Product product1;
    private Product product2;
    private Product product3;
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
        product1.setPrice(new BigDecimal("100.00"));
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
        product2.setPrice(new BigDecimal("50.00"));
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

        product3 = new Product();
        product3.setId("p3");
        product3.setTitle("Best Seller Shoe");
        product3.setPrice(new BigDecimal("10.00"));
        product3.setCategoryIds(List.of("cat1"));
        product3.setProductFilterParameters(new ArrayList<>());

        category1 = new Category();
        category1.setId("1");
        category1.setTitle("Electronics");

        category2 = new Category();
        category2.setId("2");
        category2.setTitle("Books");

        when(productRepository.findAll()).thenReturn(List.of(product1, product2, product3));
        when(filterParameterRepository.findAll()).thenReturn(List.of(colorParam, brandParam));
        when(filterParameterValueRepository.findAll()).thenReturn(List.of(redValue, greenValue, nikeValue, adidasValue));
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        // Setup sales counts
        ProductSales sales1 = new ProductSales();
        sales1.setProductId("p1");
        sales1.setSalesCount(10);
        ProductSales sales2 = new ProductSales();
        sales2.setProductId("p2");
        sales2.setSalesCount(20);
        ProductSales sales3 = new ProductSales();
        sales3.setProductId("p3");
        sales3.setSalesCount(100);

        when(productSalesRepository.findAll()).thenReturn(List.of(sales1, sales2, sales3));
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

        List<FilterParameterDto> result = productSearchEngine.getFilterParametersForCategoryWithFilter("cat1", request);

        assertEquals(2, result.size());

        FilterParameterDto brandDto = result.stream().filter(f -> f.getId().equals("brand")).findFirst().orElseThrow();
        assertEquals(2, brandDto.getValues().size()); // Should have both

        FilterParameterValueDto nikeDto = brandDto.getValues().stream().filter(v -> v.getId().equals("nike")).findFirst().orElseThrow();
        assertTrue(nikeDto.getSelected());

        FilterParameterValueDto adidasDto = brandDto.getValues().stream().filter(v -> v.getId().equals("adidas")).findFirst().orElseThrow();
        assertFalse(adidasDto.getSelected());
    }

    @Test
    void search_ShouldSortByNameAsc() {
        productSearchEngine.loadProducts();

        CategoryFilterRequest request = new CategoryFilterRequest();
        request.setSort(SortOption.NAME_ASC);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(3, result.size());
        assertEquals("Adidas Green Shoe", result.get(0).getTitle());
        assertEquals("Best Seller Shoe", result.get(1).getTitle());
        assertEquals("Nike Red Shoe", result.get(2).getTitle());
    }

    @Test
    void search_ShouldSortByNameDesc() {
        productSearchEngine.loadProducts();

        CategoryFilterRequest request = new CategoryFilterRequest();
        request.setSort(SortOption.NAME_DESC);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(3, result.size());
        assertEquals("Nike Red Shoe", result.get(0).getTitle());
        assertEquals("Best Seller Shoe", result.get(1).getTitle());
        assertEquals("Adidas Green Shoe", result.get(2).getTitle());
    }

    @Test
    void search_ShouldSortByPriceAsc() {
        productSearchEngine.loadProducts();

        CategoryFilterRequest request = new CategoryFilterRequest();
        request.setSort(SortOption.PRICE_ASC);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(3, result.size());
        assertEquals("Best Seller Shoe", result.get(0).getTitle()); // 10.00
        assertEquals("Adidas Green Shoe", result.get(1).getTitle()); // 50.00
        assertEquals("Nike Red Shoe", result.get(2).getTitle()); // 100.00
    }

    @Test
    void search_ShouldSortByPriceDesc() {
        productSearchEngine.loadProducts();

        CategoryFilterRequest request = new CategoryFilterRequest();
        request.setSort(SortOption.PRICE_DESC);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(3, result.size());
        assertEquals("Nike Red Shoe", result.get(0).getTitle()); // 100.00
        assertEquals("Adidas Green Shoe", result.get(1).getTitle()); // 50.00
        assertEquals("Best Seller Shoe", result.get(2).getTitle()); // 10.00
    }

    @Test
    void search_ShouldSortByBestSelling() {
        productSearchEngine.loadProducts();

        CategoryFilterRequest request = new CategoryFilterRequest();
        request.setSort(SortOption.BEST_SELLING);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(3, result.size());
        assertEquals("Best Seller Shoe", result.get(0).getTitle()); // 100 sales
        assertEquals("Adidas Green Shoe", result.get(1).getTitle()); // 20 sales
        assertEquals("Nike Red Shoe", result.get(2).getTitle()); // 10 sales
    }

    @Test
    void search_ShouldDefaultSortByNameAsc() {
        productSearchEngine.loadProducts();

        CategoryFilterRequest request = new CategoryFilterRequest();
        request.setSort(null);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(3, result.size());
        assertEquals("Adidas Green Shoe", result.get(0).getTitle());
        assertEquals("Best Seller Shoe", result.get(1).getTitle());
        assertEquals("Nike Red Shoe", result.get(2).getTitle());
    }

    @Test
    void search_ShouldReturnProductsInSubcategories() {
        Category cat1 = new Category();
        cat1.setId("cat1");
        cat1.setActive(true);
        cat1.setVisible(true);

        Category cat2 = new Category();
        cat2.setId("cat2");
        cat2.setParentId("cat1");
        cat2.setActive(true);
        cat2.setVisible(true);

        Product product1 = new Product();
        product1.setId("p1");
        product1.setTitle("Product in Parent");
        product1.setCategoryIds(List.of("cat1"));

        Product product2 = new Product();
        product2.setId("p2");
        product2.setTitle("Product in Child");
        product2.setCategoryIds(List.of("cat2"));

        // NOTE: In the previous setUp(), we already mocked repositories returning specific products.
        // To test this specific scenario, we need to override the mocks or create a new test setup.
        // Since the class uses @InjectMocks, we can't easily reset the internal state cleanly without partial rebuilding or
        // using a separate test method that re-initializes things or just adding these to the existing mocks if possible.
        // However, Mockito's 'when' can be overridden.

        // Let's redefine the mocks for THIS test.
        when(productRepository.findAll()).thenReturn(List.of(product1, product2));
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        // We need to reload products because ProductSearchEngine loads them @EventListener
        productSearchEngine.loadProducts();

        // Search for products in cat1 (parent)
        // Expected: p1 (direct) and p2 (indirect via cat2)
        List<Product> result = productSearchEngine.search("cat1", null);

        assertEquals(2, result.size(), "Should return products from subcategories");
    }

    @Test
    void searchCategories_ShouldReturnFilteredCategories() {
        Category cat1 = new Category();
        cat1.setId("1");
        cat1.setTitle("Electronics");

        Category cat2 = new Category();
        cat2.setId("2");
        cat2.setTitle("Books");

        // We need to reload products because ProductSearchEngine loads them @EventListener
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));
        productSearchEngine.loadProducts();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> result = productSearchEngine.searchCategories("Elec", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Electronics", result.getContent().get(0).getTitle());
    }

    @Test
    void searchCategories_ShouldReturnAllCategories_WhenQueryIsEmpty() {
        Category cat1 = new Category();
        cat1.setId("1");
        cat1.setTitle("Electronics");

        Category cat2 = new Category();
        cat2.setId("2");
        cat2.setTitle("Books");

        // We need to reload products because ProductSearchEngine loads them @EventListener
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));
        productSearchEngine.loadProducts();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> result = productSearchEngine.searchCategories("", pageable);

        assertEquals(2, result.getTotalElements());
    }
}
