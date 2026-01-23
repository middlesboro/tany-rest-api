package sk.tany.rest.api.service.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.product.ProductRepository;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEmbeddingServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductEmbeddingService productEmbeddingService;

    @Test
    void shouldInitializeInBackground() {
        // Given
        when(productRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // When
        productEmbeddingService.startBackgroundInitialization();

        // Then
        // Wait for initialization to complete (max 30 seconds to allow for model loading)
        await().atMost(Duration.ofSeconds(30)).until(() -> productEmbeddingService.isInitialized());
    }
}
