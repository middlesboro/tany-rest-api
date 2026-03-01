package sk.tany.rest.api.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import sk.tany.rest.api.config.TanyFeaturesConfig;
import sk.tany.rest.api.dto.client.customermessage.CustomerMessageCreateRequest;
import sk.tany.rest.api.dto.features.InvoiceDataDto;

import java.util.Collections;

@Service
@Slf4j
public class TanyFeaturesClient {

    private final RestClient restClient;
    private final TanyFeaturesConfig config;

    public TanyFeaturesClient(
            RestClient.Builder restClientBuilder,
            TanyFeaturesConfig config) {
        this.config = config;
        this.restClient = restClientBuilder.build();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tany-Features-Api-Key", config.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public void regenerateEmbeddings() {
        log.info("Calling tany-features to regenerate all embeddings...");
        restClient.post()
                .uri(config.getUrl() + "/api/features/embeddings/regenerate")
                .headers(h -> h.addAll(getHeaders()))
                .retrieve()
                .toBodilessEntity();
    }

    public void updateEmbedding(String productId) {
        log.debug("Calling tany-features to update embedding for product {}", productId);
        try {
            restClient.post()
                    .uri(config.getUrl() + "/api/features/embeddings/update/" + productId)
                    .headers(h -> h.addAll(getHeaders()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to update embedding for product {} via tany-features", productId, e);
        }
    }

    public String chatMessage(CustomerMessageCreateRequest request) {
        log.debug("Calling tany-features AI assistant...");
        ResponseEntity<String> response = restClient.post()
                .uri(config.getUrl() + "/api/features/chat/message")
                .headers(h -> h.addAll(getHeaders()))
                .body(request)
                .retrieve()
                .toEntity(String.class);
        return response.getBody();
    }

    public byte[] generateInvoice(InvoiceDataDto invoiceData) {
        log.info("Calling tany-features to generate invoice for order {}", invoiceData.getOrderIdentifier());
        ResponseEntity<byte[]> response = restClient.post()
                .uri(config.getUrl() + "/api/features/invoices/generate")
                .headers(h -> h.addAll(getHeaders()))
                .body(invoiceData)
                .retrieve()
                .toEntity(byte[].class);
        return response.getBody();
    }

    public byte[] generateCreditNote(InvoiceDataDto invoiceData) {
        log.info("Calling tany-features to generate credit note for order {}", invoiceData.getOrderIdentifier());
        ResponseEntity<byte[]> response = restClient.post()
                .uri(config.getUrl() + "/api/features/invoices/credit-note")
                .headers(h -> h.addAll(getHeaders()))
                .body(invoiceData)
                .retrieve()
                .toEntity(byte[].class);
        return response.getBody();
    }
}
