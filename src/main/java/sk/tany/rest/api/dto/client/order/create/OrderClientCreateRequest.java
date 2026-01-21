package sk.tany.rest.api.dto.client.order.create;

import lombok.Data;

@Data
public class OrderClientCreateRequest {
    private String cartId;
    private String note;
}
