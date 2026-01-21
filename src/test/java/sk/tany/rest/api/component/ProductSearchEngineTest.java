package sk.tany.rest.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.SortOption;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchEngineTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductSearchEngine productSearchEngine;

    private Product product1;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId("p1");
        product1.setTitle("Nike Red Shoe");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setCategoryIds(List.of("cat1"));
        product1.setProductFilterParameters(new ArrayList<>());
    }

    @Test
    void searchAndSort_ShouldDelegateToRepository() {
        when(productRepository.searchAndSort("Nike")).thenReturn(List.of(product1));

        List<Product> result = productSearchEngine.searchAndSort("Nike");

        assertEquals(1, result.size());
        assertEquals("Nike Red Shoe", result.get(0).getTitle());
        verify(productRepository).searchAndSort("Nike");
    }

    @Test
    void getFilterParametersForCategory_ShouldDelegateToRepository() {
        List<FilterParameterDto> dtos = new ArrayList<>();
        when(productRepository.getFilterParametersForCategoryWithFilter("cat1", null)).thenReturn(dtos);

        List<FilterParameterDto> result = productSearchEngine.getFilterParametersForCategory("cat1");

        assertSame(dtos, result);
        verify(productRepository).getFilterParametersForCategoryWithFilter("cat1", null);
    }

    @Test
    void getFilterParametersForCategoryWithFilter_ShouldDelegateToRepository() {
        CategoryFilterRequest request = new CategoryFilterRequest();
        List<FilterParameterDto> dtos = new ArrayList<>();
        when(productRepository.getFilterParametersForCategoryWithFilter("cat1", request)).thenReturn(dtos);

        List<FilterParameterDto> result = productSearchEngine.getFilterParametersForCategoryWithFilter("cat1", request);

        assertSame(dtos, result);
        verify(productRepository).getFilterParametersForCategoryWithFilter("cat1", request);
    }

    @Test
    void search_ShouldDelegateToRepository() {
        CategoryFilterRequest request = new CategoryFilterRequest();
        List<Product> products = List.of(product1);
        when(productRepository.search("cat1", request)).thenReturn(products);

        List<Product> result = productSearchEngine.search("cat1", request);

        assertSame(products, result);
        verify(productRepository).search("cat1", request);
    }

    @Test
    void searchCategories_ShouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = Page.empty();
        when(categoryRepository.searchCategories("Elec", pageable)).thenReturn(page);

        Page<Category> result = productSearchEngine.searchCategories("Elec", pageable);

        assertSame(page, result);
        verify(categoryRepository).searchCategories("Elec", pageable);
    }
}
