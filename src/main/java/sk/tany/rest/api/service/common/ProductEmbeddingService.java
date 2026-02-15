package sk.tany.rest.api.service.common;

import com.mongodb.client.MongoClient;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
    private final MongoClient mongoClient;

    @Value("${eshop.load-related-products:true}")
    private boolean loadRelatedProducts;

    @Value("${spring.data.mongodb.database:tany}")
    private String databaseName;

    private static final String COLLECTION_NAME = "product_embeddings";
    private static final String INDEX_NAME = "vector_index";

    private volatile EmbeddingStore<TextSegment> embeddingStore;
    private volatile EmbeddingModel embeddingModel;

    @PostConstruct
    public void initStore() {
        try {
            log.info("Initializing ProductEmbeddingService connection to MongoDB Atlas...");
            this.embeddingStore = MongoDbEmbeddingStore.builder()
                    .fromClient(mongoClient)
                    .databaseName(databaseName)
                    .collectionName(COLLECTION_NAME)
                    .indexName(INDEX_NAME)
                    .createIndex(false) // Assuming index is created in Atlas
                    .build();

            this.embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
            log.info("ProductEmbeddingService initialized (connection only).");

        } catch (Exception e) {
            log.error("Failed to initialize ProductEmbeddingService connection", e);
        }
    }

    @Async
    public void reEmbedAllProducts() {
        if (!isInitialized()) {
            log.warn("ProductEmbeddingService not initialized. Cannot re-embed products.");
            return;
        }

        log.info("Starting batch re-embedding of all products...");
        try {
            List<Product> products = productRepository.findAll();
            int count = 0;
            for (Product product : products) {
                if (product.isActive() && product.getTitle() != null) {
                    addInternal(product);
                    count++;
                }
            }
            log.info("Finished re-embedding {} active products.", count);
        } catch (Exception e) {
            log.error("Error during batch re-embedding of products", e);
        }
    }

    @Async
    public void updateProduct(Product product) {
        if (!isInitialized()) {
            log.warn("ProductEmbeddingService not initialized, skipping update for product: {}", product.getId());
            return;
        }

        try {
             if (product.isActive() && product.getTitle() != null) {
                addInternal(product);
                log.debug("Updated embedding for product: {}", product.getId());
             }
        } catch (Exception e) {
            log.error("Failed to update embedding for product: " + product.getId(), e);
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

        Response<dev.langchain4j.data.embedding.Embedding> embeddingResponse = embeddingModel.embed(segment);
        // Use product ID as the document ID for upsert/replacement effect
        embeddingStore.add(product.getId(), embeddingResponse.content());
    }

    public List<String> findRelatedProducts(String productId) {
        if (embeddingStore == null || embeddingModel == null) {
            // Only warn periodically or just debug to avoid log spam if service is disabled
            log.debug("Embedding store not initialized.");
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
                    EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                            .queryEmbedding(embeddingResponse.content())
                            .maxResults(6)
                            .build();
                    EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
                    List<EmbeddingMatch<TextSegment>> relevant = searchResult.matches();

                    List<String> resultIds = new ArrayList<>();
                    for (EmbeddingMatch<TextSegment> match : relevant) {
                        String matchId = match.embeddingId();
                        if (matchId != null && !matchId.equals(productId)) {
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
