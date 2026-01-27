package sk.tany.rest.api.dto.admin.cartdiscount.create;

import lombok.Data;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartDiscountCreateRequest {
    private String title;
    private String description;
    private String code;
    private DiscountType discountType;
    private BigDecimal value;
    private boolean freeShipping;
    private Instant dateFrom;
    private Instant dateTo;
    private List<String> categoryIds;
    private List<String> productIds;
    private List<String> brandIds;
    private boolean active;
    private boolean automatic;
}
