package sk.tany.rest.api.service.chat;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CrossSellTools {

    private final ProductSearchEngine productSearchEngine;

    @Tool("Search for products suitable for cross-sell. Returns a list of products matching the query. " +
            "The query can be a product title, category name, or keywords. " +
            "Only returns products that are in stock (quantity > 0).")
    public List<Product> searchProducts(String query) {
        return productSearchEngine.searchAndSort(query, true).stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0)
                .toList();
    }
}
