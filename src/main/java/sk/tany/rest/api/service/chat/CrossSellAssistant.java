package sk.tany.rest.api.service.chat;

import sk.tany.rest.api.dto.CrossSellProductsResponse;

import java.util.List;

/**
 * Common interface for Cross-Sell Assistant implementations.
 */
public interface CrossSellAssistant {

    CrossSellProductsResponse findCrossSellProducts(String productTitle, List<String> excludeIds);
}
