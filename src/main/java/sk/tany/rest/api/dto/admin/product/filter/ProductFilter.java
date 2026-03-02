package sk.tany.rest.api.dto.admin.product.filter;

import java.math.BigDecimal;

public record ProductFilter(String query,
                            BigDecimal priceFrom,
                            BigDecimal priceTo,
                            String brandId,
                            String id,
                            Boolean externalStock,
                            Integer quantity,
                            Long productIdentifier,
                            Boolean active) {
}
