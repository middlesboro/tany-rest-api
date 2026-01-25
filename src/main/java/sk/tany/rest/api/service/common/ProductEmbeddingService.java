package sk.tany.rest.api.service.common;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.bge.small.en.v15.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingService {

    private final ProductRepository productRepository;

    @Value("${eshop.load-related-products:true}")
    private boolean loadRelatedProducts;

    private volatile EmbeddingStore<TextSegment> embeddingStore;
    private volatile EmbeddingModel embeddingModel;

    @EventListener(ApplicationReadyEvent.class)
    public void startBackgroundInitialization() {
        if (!loadRelatedProducts) {
            log.info("ProductEmbeddingService initialization skipped by configuration.");
            return;
        }
        new Thread(this::init).start();
    }

    private void init() {
        log.info("Initializing ProductEmbeddingService in background...");
        try {
            // Use temporary variables to ensure atomic visibility if we were checking "isInitialized" by null checks on fields,
            // but we are assigning them one by one. Since we use them together, it's safer to check both or have a separate flag.
            // But since findRelatedProducts checks both for null, it is safe enough.
            this.embeddingStore = new InMemoryEmbeddingStore<>();
            this.embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

            List<Product> products = productRepository.findAll();
            log.info("Found {} products to embed.", products.size());

            int count = 0;
            for (Product product : products) {
                if (product.isActive() && product.getTitle() != null) {
                    addInternal(product);
                    count++;
                }
            }
            log.info("Embedded {} active products into the store. Initialization complete.", count);

        } catch (Exception e) {
            log.error("Failed to initialize ProductEmbeddingService", e);
        }
    }

    private void addInternal(Product product) {
        String text = product.getTitle();
        if (product.getDescription() != null) {
            text += " " + product.getDescription();
        } else if (product.getShortDescription() != null) {
             text += " " + product.getShortDescription();
        }

        text = text.length() > 1200 ? text.substring(0, 1200) : text;

        Metadata metadata = Metadata.from("id", product.getId());
        TextSegment segment = TextSegment.from(text, metadata);

        // Embed and add
        Response<dev.langchain4j.data.embedding.Embedding> embeddingResponse = embeddingModel.embed(segment);
        embeddingStore.add(embeddingResponse.content(), segment);
    }

    public List<String> findRelatedProducts(String productId) {
        if (embeddingStore == null || embeddingModel == null) {
            log.warn("Embedding store not initialized yet.");
            return List.of();
        }

        return productRepository.findById(productId)
                .map(product -> {
                    String text = product.getTitle();
                    if (product.getDescription() != null) {
                        text += " " + product.getDescription();
                    } else if (product.getShortDescription() != null) {
                         text += " " + product.getShortDescription();
                    }

                    text = text.length() > 1200 ? text.substring(0, 1200) : text;

                    Response<dev.langchain4j.data.embedding.Embedding> embeddingResponse = embeddingModel.embed(text);

                    // Find 6 relevant to include self (potentially) and get 5 others
                    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(embeddingResponse.content(), 6);

                    List<String> resultIds = new ArrayList<>();
                    for (EmbeddingMatch<TextSegment> match : relevant) {
                        String matchId = match.embedded().metadata().get("id");
                        if (!matchId.equals(productId)) {
                            resultIds.add(matchId);
                        }
                    }

                    // limit to 5 just in case
                    if (resultIds.size() > 5) {
                        return resultIds.subList(0, 5);
                    }
                    return resultIds;
                })
                .orElse(List.of());
    }

    public boolean isInitialized() {
        return embeddingStore != null && embeddingModel != null;
    }
}
