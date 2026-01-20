package sk.tany.rest.api.domain.cartdiscount;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "cart_discounts")
public class CartDiscount {

    @Id
    private String id;
    private String title;
    private String code; // If null, applied automatically
    private DiscountType discountType;
    private BigDecimal value;
    private boolean freeShipping;
    private Instant dateFrom;
    private Instant dateTo;

    // Conditions
    private List<String> categoryIds;
    private List<String> productIds;
    private List<String> brandIds;

    private boolean active = true;

    @CreatedDate
    private Instant createDate;
    @LastModifiedDate
    private Instant updateDate;
}
