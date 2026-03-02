package sk.tany.rest.api.service.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import com.mongodb.client.MongoClient;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEmbeddingServiceTest {

    @Mock
    private sk.tany.rest.api.client.TanyFeaturesClient tanyFeaturesClient;

    @InjectMocks
    private ProductEmbeddingService productEmbeddingService;

    @Test
    void shouldSkipUpdateIfNotInitialized() {
        Product product = new Product();
        product.setId("1");

        productEmbeddingService.updateProduct(product);

        verify(tanyFeaturesClient, times(1)).updateEmbedding("1");
    }
}
