package sk.tany.rest.api.domain.product;

import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryTest {

    @Mock private Nitrite nitrite;
    @Mock private FilterParameterRepository filterParameterRepository;
    @Mock private FilterParameterValueRepository filterParameterValueRepository;
    @Mock private ProductSalesRepository productSalesRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FilterParameterMapper filterParameterMapper;
    @Mock private FilterParameterValueMapper filterParameterValueMapper;

    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository(
                nitrite,
                filterParameterRepository,
                filterParameterValueRepository,
                productSalesRepository,
                categoryRepository,
                filterParameterMapper,
                filterParameterValueMapper
        ) {
            @Override
            public void init() {
                // skip nitrite init
            }
            @Override
            public Product save(Product entity) {
                memoryCache.put(entity.getId(), entity);
                return entity;
            }
        };
    }

    @Test
    void findAllByBrandId_shouldReturnOnlyProductsForGivenBrand() {
        Product p1 = new Product();
        p1.setId("1");
        p1.setBrandId("brand1");

        Product p2 = new Product();
        p2.setId("2");
        p2.setBrandId("brand2");

        Product p3 = new Product();
        p3.setId("3");
        p3.setBrandId("brand1");

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        List<Product> result = productRepository.findAllByBrandId("brand1");

        assertThat(result).hasSize(2)
                .extracting(Product::getId)
                .containsExactlyInAnyOrder("1", "3");
    }
}
