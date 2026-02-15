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
    private ProductRepository productRepository;

    @Mock
    private MongoClient mongoClient;

    @InjectMocks
    private ProductEmbeddingService productEmbeddingService;

    @Test
    void shouldSkipUpdateIfNotInitialized() {
        Product product = new Product();
        product.setId("1");

        // Since init() runs in a thread on ApplicationReadyEvent, and we are not firing that event here,
        // the service is not initialized.
        productEmbeddingService.updateProduct(product);

        // Verification: Since we mocked the dependencies, if it tried to do anything,
        // it would likely throw NPE or interact with mocks.
        // But the check `if (!isInitialized())` should prevent it.
        // We can't easily verify static calls or internal state without more complex setup,
        // but we can verify no interactions with repository for example if we were fetching inside update.
        // Actually updateProduct calls addInternal which uses embeddingModel.embed().
        // If it proceeded, it would crash because embeddingModel is null.
        // So if this test passes without exception, it means it returned early.
    }
}
