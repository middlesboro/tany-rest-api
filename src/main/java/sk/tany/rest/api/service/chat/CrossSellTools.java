package sk.tany.rest.api.service.chat;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.dto.CrossSellProductDto;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossSellTools {

    private final ProductSearchEngine productSearchEngine;

    @Tool("Search for products suitable for cross-sell. Returns a list of products matching the query. " +
            "The query can be a product title, category name, or keywords. " +
            "Only returns products that are in stock (quantity > 0).")
    public List<CrossSellProductDto> searchProducts(String query, List<String> excludeIds) {
        List<Product> products = productSearchEngine.searchAndSort(query, true).stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && !excludeIds.contains(p.getId()))
                .toList();

        return products.stream()
                .map(p -> {
                    CrossSellProductDto dto = new CrossSellProductDto();
                    dto.setId(p.getId());
                    dto.setTitle(p.getTitle());
                    dto.setSlug(p.getSlug());
                    dto.setPrice(p.getDiscountPrice() != null ? p.getDiscountPrice() : p.getPrice());
                    if (p.getImages() != null && !p.getImages().isEmpty()) {
                        dto.setImage(p.getImages().getFirst());
                    }
                    return dto;
                })
                .toList();
    }

}
