package sk.tany.rest.api.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.client.TanyFeaturesClient;

import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingService {

    private final TanyFeaturesClient tanyFeaturesClient;

    @Async
    public void reEmbedAllProducts() {
        try {
            tanyFeaturesClient.regenerateEmbeddings();
        } catch (Exception e) {
            log.error("Error triggering embedding regeneration via tany-features", e);
        }
    }

    @Async
    public void updateProduct(Product product) {
        if (product != null && product.getId() != null) {
            tanyFeaturesClient.updateEmbedding(product.getId());
        }
    }

    public List<ProductClientDto> findRelatedProducts(String productId) {
        log.warn("findRelatedProducts is not supported directly when extracting embeddings to tany-features. Ensure the caller implementation is updated or an endpoint is provided.");
        return Collections.emptyList();
    }
}
