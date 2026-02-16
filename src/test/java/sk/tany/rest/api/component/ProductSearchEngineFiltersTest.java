package sk.tany.rest.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterType;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.FilterParameterRequest;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;
import sk.tany.rest.api.mapper.ProductLabelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchEngineFiltersTest {

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
    @Mock
    private sk.tany.rest.api.domain.productlabel.ProductLabelRepository productLabelRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ProductLabelMapper productLabelMapper;

    @InjectMocks
    private ProductSearchEngine productSearchEngine;

    private Product productNike;
    private Product productAdidas;
    private Product productNoBrand;
    private Brand brandNike;
    private Brand brandAdidas;

    @BeforeEach
    void setUp() {
        brandNike = new Brand();
        brandNike.setId("b_nike");
        brandNike.setName("Nike");

        brandAdidas = new Brand();
        brandAdidas.setId("b_adidas");
        brandAdidas.setName("Adidas");

        productNike = new Product();
        productNike.setId("p1");
        productNike.setTitle("Nike Shoe");
        productNike.setBrandId("b_nike");
        productNike.setCategoryIds(List.of("cat1"));
        productNike.setQuantity(10); // ON_STOCK

        productAdidas = new Product();
        productAdidas.setId("p2");
        productAdidas.setTitle("Adidas Shoe");
        productAdidas.setBrandId("b_adidas");
        productAdidas.setCategoryIds(List.of("cat1"));
        productAdidas.setQuantity(0); // SOLD_OUT

        productNoBrand = new Product();
        productNoBrand.setId("p3");
        productNoBrand.setTitle("Unknown Shoe");
        productNoBrand.setCategoryIds(List.of("cat1"));
        productNoBrand.setQuantity(null); // SOLD_OUT treated as null quantity? Logic says <= 0 or null.

        Category category = new Category();
        category.setId("cat1");

        when(productRepository.findAll()).thenReturn(List.of(productNike, productAdidas, productNoBrand));
        when(brandRepository.findAll()).thenReturn(List.of(brandNike, brandAdidas));
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        // Mock other repositories to avoid NPEs during load
        when(filterParameterRepository.findAll()).thenReturn(List.of());
        when(filterParameterValueRepository.findAll()).thenReturn(List.of());
        when(productSalesRepository.findAll()).thenReturn(List.of());
        when(productLabelRepository.findAll()).thenReturn(List.of());

        productSearchEngine.loadProducts();
    }

    @Test
    void search_ShouldFilterByBrand() {
        CategoryFilterRequest request = new CategoryFilterRequest();
        FilterParameterRequest brandFilter = new FilterParameterRequest();
        brandFilter.setId("BRAND");
        brandFilter.setFilterParameterValueIds(List.of("Nike"));
        request.setFilterParameters(List.of(brandFilter));

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(1, result.size());
        assertEquals("Nike Shoe", result.getFirst().getTitle());
    }

    @Test
    void search_ShouldFilterByBrand_Multiple() {
        CategoryFilterRequest request = new CategoryFilterRequest();
        FilterParameterRequest brandFilter = new FilterParameterRequest();
        brandFilter.setId("BRAND");
        brandFilter.setFilterParameterValueIds(List.of("Nike", "Adidas"));
        request.setFilterParameters(List.of(brandFilter));

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getTitle().equals("Nike Shoe")));
        assertTrue(result.stream().anyMatch(p -> p.getTitle().equals("Adidas Shoe")));
    }

    @Test
    void search_ShouldFilterByAvailability_OnStock() {
        CategoryFilterRequest request = new CategoryFilterRequest();
        FilterParameterRequest availFilter = new FilterParameterRequest();
        availFilter.setId("AVAILABILITY");
        availFilter.setFilterParameterValueIds(List.of("ON_STOCK"));
        request.setFilterParameters(List.of(availFilter));

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(1, result.size());
        assertEquals("Nike Shoe", result.getFirst().getTitle());
    }

    @Test
    void search_ShouldFilterByAvailability_SoldOut() {
        CategoryFilterRequest request = new CategoryFilterRequest();
        FilterParameterRequest availFilter = new FilterParameterRequest();
        availFilter.setId("AVAILABILITY");
        availFilter.setFilterParameterValueIds(List.of("SOLD_OUT"));
        request.setFilterParameters(List.of(availFilter));

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getTitle().equals("Adidas Shoe")));
        assertTrue(result.stream().anyMatch(p -> p.getTitle().equals("Unknown Shoe"))); // Quantity null treated as sold out
    }

    @Test
    void search_ShouldFilterByBrandAndAvailability() {
        CategoryFilterRequest request = new CategoryFilterRequest();

        FilterParameterRequest brandFilter = new FilterParameterRequest();
        brandFilter.setId("BRAND");
        brandFilter.setFilterParameterValueIds(List.of("Adidas"));

        FilterParameterRequest availFilter = new FilterParameterRequest();
        availFilter.setId("AVAILABILITY");
        availFilter.setFilterParameterValueIds(List.of("SOLD_OUT"));

        request.setFilterParameters(List.of(brandFilter, availFilter));

        List<Product> result = productSearchEngine.search("cat1", request);

        assertEquals(1, result.size());
        assertEquals("Adidas Shoe", result.getFirst().getTitle());
    }

    @Test
    void getFilterParameters_ShouldReturnBrandAndAvailability() {
        CategoryFilterRequest request = new CategoryFilterRequest(); // Empty request

        List<FilterParameterDto> facets = productSearchEngine.getFilterParametersForCategoryWithFilter("cat1", request);

        // Check Brand
        FilterParameterDto brandFacet = facets.stream()
                .filter(f -> "BRAND".equals(f.getId()))
                .findFirst().orElseThrow(() -> new AssertionError("BRAND facet missing"));
        assertEquals(FilterParameterType.BRAND, brandFacet.getType());
        assertEquals(2, brandFacet.getValues().size());
        assertTrue(brandFacet.getValues().stream().anyMatch(v -> v.getId().equals("Nike")));
        assertTrue(brandFacet.getValues().stream().anyMatch(v -> v.getId().equals("Adidas")));

        // Check Availability
        FilterParameterDto availFacet = facets.stream()
                .filter(f -> "AVAILABILITY".equals(f.getId()))
                .findFirst().orElseThrow(() -> new AssertionError("AVAILABILITY facet missing"));
        assertEquals(FilterParameterType.AVAILABILITY, availFacet.getType());
        assertEquals(2, availFacet.getValues().size());
        assertTrue(availFacet.getValues().stream().anyMatch(v -> v.getId().equals("ON_STOCK")));
        assertTrue(availFacet.getValues().stream().anyMatch(v -> v.getId().equals("SOLD_OUT")));
    }
}
