package sk.tany.rest.api.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class PriceCalculator {

    private final ShopSettingsRepository shopSettingsRepository;

    public BigDecimal calculatePriceWithoutVat(BigDecimal priceWithVat) {
        if (priceWithVat == null) return null;
        BigDecimal vatPercentage = shopSettingsRepository.getFirstShopSettings().getVat();
        if (vatPercentage == null || vatPercentage.compareTo(BigDecimal.ZERO) == 0) return priceWithVat.setScale(2, RoundingMode.HALF_UP);
        BigDecimal divisor = BigDecimal.ONE.add(vatPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return priceWithVat.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculatePriceWithVat(BigDecimal priceWithoutVat) {
        if (priceWithoutVat == null) return null;
        BigDecimal vatPercentage = shopSettingsRepository.getFirstShopSettings().getVat();
        if (vatPercentage == null || vatPercentage.compareTo(BigDecimal.ZERO) == 0) return priceWithoutVat.setScale(2, RoundingMode.HALF_UP);
        BigDecimal multiplier = BigDecimal.ONE.add(vatPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return priceWithoutVat.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundPrice(BigDecimal price) {
        if (price == null) return null;
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
