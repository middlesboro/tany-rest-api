package sk.tany.rest.api.dto.admin.cartdiscount.update;

import lombok.Data;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartDiscountUpdateRequest {
    private String title;
    private String description;
    private String code;
    private DiscountType discountType;
    private BigDecimal value;
    private Instant dateFrom;
    private Instant dateTo;
    private List<String> categoryIds;
    private List<String> productIds;
    private List<String> brandIds;
    private boolean active;
    private boolean automatic;
}
