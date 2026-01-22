package sk.tany.rest.api.service.common;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.bge.small.en.v15.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private EmbeddingStore<TextSegment> embeddingStore;
    private EmbeddingModel embeddingModel;

    @PostConstruct
    public void init() {
        // Initialization can be heavy, run in background to not block startup if possible,
        // but user requested "on startup load". We will do it synchronously to ensure it's ready.
        // However, loading model and embedding all products might take time.
        // BgeSmallEnV15QuantizedEmbeddingModel loads via ONNX.

        log.info("Initializing ProductEmbeddingService...");
        try {
            this.embeddingStore = new InMemoryEmbeddingStore<>();
            this.embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

            List<Product> products = productRepository.findAll();
            log.info("Found {} products to embed.", products.size());

            // We can process in batches if needed, but for "all on startup" let's just loop.
            int count = 0;
            for (Product product : products) {
                if (product.isActive() && product.getTitle() != null) {
                    addInternal(product);
                    count++;
                }
            }
            log.info("Embedded {} active products into the store.", count);

        } catch (Exception e) {
            log.error("Failed to initialize ProductEmbeddingService", e);
        }
    }

    private void addInternal(Product product) {
        String text = product.getTitle();
        if (product.getDescription() != null) {
            // Truncate description if too long to avoid token limits, though BGE small handles reasonable length.
            // Simple concatenation for now.
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
            log.warn("Embedding store not initialized.");
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
}
