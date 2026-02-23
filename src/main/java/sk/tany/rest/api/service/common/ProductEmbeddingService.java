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
import sk.tany.rest.api.dto.client.product.ProductClientDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingService {

    private final ProductRepository productRepository;
    private final MongoClient mongoClient;

    private final sk.tany.rest.api.config.MongoDbConfigProperties mongoDbConfigProperties;
    private final sk.tany.rest.api.config.EshopConfig eshopConfig;

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
                    .databaseName(mongoDbConfigProperties.getDatabase())
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

        Metadata metadata = new Metadata();
        metadata.put("id", product.getId());
        metadata.put("title", product.getTitle());
        if (product.getPrice() != null) metadata.put("price", product.getPrice().toString());
        if (product.getDiscountPrice() != null) metadata.put("discountPrice", product.getDiscountPrice().toString());
        if (product.getQuantity() != null) metadata.put("quantity", String.valueOf(product.getQuantity()));
        if (product.getSlug() != null) metadata.put("slug", product.getSlug());
        if (product.getAverageRating() != null) metadata.put("averageRating", product.getAverageRating().toString());
        if (product.getReviewsCount() != null) metadata.put("reviewsCount", String.valueOf(product.getReviewsCount()));
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            metadata.put("image", product.getImages().getFirst());
        }

        TextSegment segment = TextSegment.from(text, metadata);

        Response<dev.langchain4j.data.embedding.Embedding> embeddingResponse = embeddingModel.embed(segment);
        // first remove old embedding if exists, then add new one
        embeddingStore.remove(product.getId());
        embeddingStore.add(product.getId(), embeddingResponse.content());
    }

    public List<ProductClientDto> findRelatedProducts(String productId) {
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

                    List<ProductClientDto> results = new ArrayList<>();
                    for (EmbeddingMatch<TextSegment> match : relevant) {
                        String matchId = match.embeddingId();
                        if (matchId != null && !matchId.equals(productId)) {
                            Metadata m = match.embedded().metadata();
                            ProductClientDto dto = new ProductClientDto();
                            dto.setId(m.getString("id"));
                            dto.setTitle(m.getString("title"));
                            if (m.getString("price") != null) dto.setPrice(new BigDecimal(m.getString("price")));
                            if (m.getString("discountPrice") != null) dto.setDiscountPrice(new BigDecimal(m.getString("discountPrice")));
                            if (m.getString("quantity") != null) dto.setQuantity(Integer.parseInt(m.getString("quantity")));
                            if (m.getString("slug") != null) dto.setSlug(m.getString("slug"));
                            if (m.getString("averageRating") != null) dto.setAverageRating(new BigDecimal(m.getString("averageRating")));
                            if (m.getString("reviewsCount") != null) dto.setReviewsCount(Integer.parseInt(m.getString("reviewsCount")));
                            if (m.getString("image") != null) dto.setImages(List.of(m.getString("image")));
                            results.add(dto);
                        }
                    }

                    // limit to 5 just in case
                    if (results.size() > 5) {
                        return results.subList(0, 5);
                    }
                    return results;
                })
                .orElse(List.of());
    }

    public boolean isInitialized() {
        return embeddingStore != null && embeddingModel != null;
    }
}
