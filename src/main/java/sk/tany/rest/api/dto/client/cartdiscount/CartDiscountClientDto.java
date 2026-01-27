package sk.tany.rest.api.dto.client.cartdiscount;

import lombok.Data;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;

import java.math.BigDecimal;

@Data
public class CartDiscountClientDto {
    private String code;
    private String title;
    private DiscountType discountType;
    private BigDecimal value;
}
