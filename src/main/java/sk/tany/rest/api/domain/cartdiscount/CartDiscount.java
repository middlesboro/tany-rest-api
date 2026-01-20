package sk.tany.rest.api.domain.cartdiscount;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartDiscount {

    @Id
    private String id;
    private String code;
    private String title;
    private BigDecimal value;
    private boolean percentage;
    private boolean active;
    private boolean freeShipping;
    private BigDecimal minOrderPrice;
    private Instant dateFrom;
    private Instant dateTo;
    private List<String> categoryIds;
    private List<String> productIds;
    private List<String> brandIds;
    private DiscountType discountType;
    private Instant createDate;
    private Instant updateDate;
}
