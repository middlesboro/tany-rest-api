package sk.tany.rest.api.service.admin.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.admin.review.ReviewAdminCreateRequest;
import sk.tany.rest.api.dto.admin.review.ReviewAdminDetailResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminListResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminUpdateRequest;
import sk.tany.rest.api.service.admin.ReviewAdminService;
import sk.tany.rest.api.service.mapper.ReviewMapper;
import sk.tany.rest.api.exception.ReviewException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAdminServiceImpl implements ReviewAdminService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    @Override
    public Page<ReviewAdminListResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toAdminListResponse);
    }

    @Override
    public ReviewAdminDetailResponse findById(String id) {
        return repository.findById(id)
                .map(mapper::toAdminDetailResponse)
                .orElseThrow(() -> new ReviewException.NotFound("Review not found"));
    }

    @Override
    public ReviewAdminDetailResponse create(ReviewAdminCreateRequest request) {
        Review review = mapper.toEntity(request);
        Review saved = repository.save(review);
        return mapper.toAdminDetailResponse(saved);
    }

    @Override
    public ReviewAdminDetailResponse update(String id, ReviewAdminUpdateRequest request) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ReviewException.NotFound("Review not found"));
        mapper.updateEntityFromRequest(request, review);
        Review saved = repository.save(review);
        return mapper.toAdminDetailResponse(saved);
    }

    @Override
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ReviewException.NotFound("Review not found");
        }
        repository.deleteById(id);
    }

    @Override
    public void importReviews() {
        try {
            ClassPathResource resource = new ClassPathResource("reviews.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);

            JsonNode dataNode = null;
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("type") && "table".equals(node.get("type").asText()) &&
                            node.has("name") && "ps_gsnipreview".equals(node.get("name").asText())) {
                        dataNode = node.get("data");
                        break;
                    }
                }
            }

            if (dataNode != null && dataNode.isArray()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (JsonNode item : dataNode) {
                    try {
                        Review review = new Review();
                        Long prestashopProductId = Long.valueOf(item.get("id_product").asText());
                        review.setPrestashopProductId(prestashopProductId);

                        Optional<Product> productOptional = productRepository.findByPrestashopId(prestashopProductId);
                        if (productOptional.isPresent()) {
                            Product product = productOptional.get();
                            review.setProductId(product.getId());
                            review.setText(item.get("text_review").asText());
                            review.setRating(Integer.parseInt(item.get("rating").asText()));
                            // Mapping title from "title_review"
                            if (item.has("title_review")) {
                                review.setTitle(item.get("title_review").asText());
                            }
                            review.setEmail(item.get("email").asText());
                            review.setActive("1".equals(item.get("is_active").asText()));
                            review.setCustomerName(item.get("customer_name").asText());

                            if (item.has("time_add")) {
                                String timeAdd = item.get("time_add").asText();
                                try {
                                    LocalDateTime ldt = LocalDateTime.parse(timeAdd, formatter);
                                    review.setCreateDate(ldt.atZone(ZoneId.systemDefault()).toInstant());
                                } catch (Exception e) {
                                    log.warn("Failed to parse date for review {}: {}", prestashopProductId, timeAdd);
                                }
                            }

                            repository.save(review);
                        }
                    } catch (Exception e) {
                        log.error("Failed to import review item: {}", item, e);
                    }
                }
            }

        } catch (IOException e) {
            throw new ReviewException("Failed to import reviews", e);
        }
    }
}
