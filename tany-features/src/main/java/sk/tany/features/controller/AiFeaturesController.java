package sk.tany.features.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tany.features.domain.product.Product;
import sk.tany.features.domain.product.ProductRepository;
import sk.tany.features.dto.CustomerMessageCreateRequest;
import sk.tany.features.service.chat.OrderAssistant;
import sk.tany.features.service.common.ProductEmbeddingService;

@Slf4j
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class AiFeaturesController {

    private final ProductEmbeddingService productEmbeddingService;
    private final ProductRepository productRepository;
    private final OrderAssistant orderAssistant;

    @PostMapping("/embeddings/regenerate")
    public ResponseEntity<Void> regenerateEmbeddings() {
        productEmbeddingService.reEmbedAllProducts();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/embeddings/update/{productId}")
    public ResponseEntity<Void> updateEmbedding(@PathVariable String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            productEmbeddingService.updateProduct(product);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/chat/message")
    public ResponseEntity<String> chatMessage(@RequestBody CustomerMessageCreateRequest request) {
        String response = orderAssistant.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
