package sk.tany.rest.api.dto.admin.cart.list;

import lombok.Data;

import java.time.Instant;

@Data
public class CartAdminListResponse {
    private String customerName;
    private String customerId;
    private String cartId;
    private Instant createDate;
    private Instant updateDate;
}
