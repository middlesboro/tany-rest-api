package sk.tany.rest.api.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.service.common.ProductEmbeddingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingEventListener extends AbstractMongoEventListener<Product> {

    private final ProductEmbeddingService productEmbeddingService;

    @Override
    public void onAfterSave(AfterSaveEvent<Product> event) {
        Product product = event.getSource();
        log.debug("Triggering background embedding update for product: {}", product.getId());
        productEmbeddingService.updateProduct(product);
    }
}
