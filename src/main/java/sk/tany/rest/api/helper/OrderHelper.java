package sk.tany.rest.api.helper;

import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierPriceRange;
import sk.tany.rest.api.dto.ProductDto;

import java.math.BigDecimal;
import java.util.List;

public class OrderHelper {

    private OrderHelper() {
    }

    public static BigDecimal getProductsWeight(List<ProductDto> products) {
        return products.stream()
                .map(p -> (p.getWeight() != null ? p.getWeight() : BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(p.getQuantity() != null ? p.getQuantity() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal getCarrierPrice(Carrier carrier, BigDecimal weight) {
        if (carrier.getRanges() != null) {
            CarrierPriceRange priceRange = carrier.getRanges().stream()
                    .filter(range ->
                            (range.getWeightFrom() == null || weight.compareTo(range.getWeightFrom()) >= 0) &&
                                    (range.getWeightTo() == null || weight.compareTo(range.getWeightTo()) <= 0)
                    )
                    .findFirst().orElse(null);

            return priceRange != null && priceRange.getPrice() != null ? priceRange.getPrice() : null;
        }

        return null;
    }

    public static BigDecimal getCarrierPrice(sk.tany.rest.api.dto.CarrierDto carrier, BigDecimal weight) {
        if (carrier.getRanges() != null) {
            sk.tany.rest.api.dto.CarrierPriceRangeDto priceRange = carrier.getRanges().stream()
                    .filter(range ->
                            (range.getWeightFrom() == null || weight.compareTo(range.getWeightFrom()) >= 0) &&
                                    (range.getWeightTo() == null || weight.compareTo(range.getWeightTo()) <= 0)
                    )
                    .findFirst().orElse(null);

            return priceRange != null && priceRange.getPrice() != null ? priceRange.getPrice() : null;
        }

        return null;
    }

    public static BigDecimal getProductsPrice(List<ProductDto> products) {
        return products.stream().map(ProductDto::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
