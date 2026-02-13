package sk.tany.rest.api.domain.cartdiscount;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartDiscount implements BaseEntity {

    @Id
    private String id;
    private String code;
    private String title;
    private String description;
    private BigDecimal value;
    private boolean percentage;
    private boolean active;
    private boolean automatic;
    private BigDecimal minOrderPrice;
    private Instant dateFrom;
    private Instant dateTo;
    private List<String> categoryIds;
    private List<String> productIds;
    private List<String> brandIds;
    private DiscountType discountType;
    private Instant createDate;
    private Instant updateDate;

    @Override
    public void setCreatedDate(Instant date) {
        this.createDate = date;
    }
    @Override
    public Instant getCreatedDate() {
        return this.createDate;
    }
    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
