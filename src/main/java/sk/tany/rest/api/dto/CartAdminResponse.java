package sk.tany.rest.api.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CartAdminResponse {
    private String customerName;
    private String customerId;
    private String cartId;
    private Instant createDate;
    private Instant updateDate;
}
