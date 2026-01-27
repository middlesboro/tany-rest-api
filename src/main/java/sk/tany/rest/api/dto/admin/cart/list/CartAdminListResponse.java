package sk.tany.rest.api.dto.admin.cart.list;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CartAdminListResponse {
    private String customerName;
    private String customerId;
    private String cartId;
    private Long orderIdentifier;
    private BigDecimal price;
    private String carrierName;
    private String paymentName;
    private Instant createDate;
    private Instant updateDate;
}
