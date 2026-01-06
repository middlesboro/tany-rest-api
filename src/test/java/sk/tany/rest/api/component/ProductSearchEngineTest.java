package sk.tany.rest.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchEngineTest {

    @Mock
    private ProductRepository productRepository;

    private ProductSearchEngine productSearchEngine;

    @BeforeEach
    void setUp() {
        productSearchEngine = new ProductSearchEngine(productRepository);
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
}
