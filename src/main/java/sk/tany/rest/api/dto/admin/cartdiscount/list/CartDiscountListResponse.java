package sk.tany.rest.api.dto.admin.cartdiscount.list;

import lombok.Data;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CartDiscountListResponse {
    private String id;
    private String title;
    private String code;
    private DiscountType discountType;
    private BigDecimal value;
    private Instant dateFrom;
    private Instant dateTo;
    private boolean active;
}
