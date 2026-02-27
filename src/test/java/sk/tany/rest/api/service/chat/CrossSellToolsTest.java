package sk.tany.rest.api.service.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.dto.CrossSellProductDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrossSellToolsTest {

    @Mock
    private ProductSearchEngine productSearchEngine;

    @InjectMocks
    private CrossSellTools crossSellTools;

    @Test
    void searchProducts_shouldReturnOnlyStockProducts() {
        Product p1 = new Product();
        p1.setId("1");
        p1.setQuantity(10);

        Product p2 = new Product();
        p2.setId("2");
        p2.setQuantity(0);

        Product p3 = new Product();
        p3.setId("3");
        p3.setQuantity(null);

        when(productSearchEngine.searchAndSort("query", true)).thenReturn(List.of(p1, p2, p3));

        List<CrossSellProductDto> result = crossSellTools.searchProducts("query", List.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
    }
}
