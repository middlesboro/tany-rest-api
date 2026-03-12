package sk.tany.rest.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceCalculator {

    public static BigDecimal calculatePriceWithoutVat(BigDecimal priceWithVat, BigDecimal vatPercentage) {
        if (priceWithVat == null) return null;
        if (vatPercentage == null || vatPercentage.compareTo(BigDecimal.ZERO) == 0) return priceWithVat.setScale(2, RoundingMode.HALF_UP);
        BigDecimal divisor = BigDecimal.ONE.add(vatPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return priceWithVat.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculatePriceWithVat(BigDecimal priceWithoutVat, BigDecimal vatPercentage) {
        if (priceWithoutVat == null) return null;
        if (vatPercentage == null || vatPercentage.compareTo(BigDecimal.ZERO) == 0) return priceWithoutVat.setScale(2, RoundingMode.HALF_UP);
        BigDecimal multiplier = BigDecimal.ONE.add(vatPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return priceWithoutVat.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundPrice(BigDecimal price) {
        if (price == null) return null;
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
