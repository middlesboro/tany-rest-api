package sk.tany.rest.api.domain.product;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ProductStatusTest {

    @Test
    void testEnumValues() {
        assertThat(ProductStatus.AVAILABLE).isNotNull();
        assertThat(ProductStatus.AVAILABLE_ON_EXTERNAL_STOCK).isNotNull();
        assertThat(ProductStatus.SOLD_OUT).isNotNull();
        assertThat(ProductStatus.values()).hasSize(3);
    }
}
