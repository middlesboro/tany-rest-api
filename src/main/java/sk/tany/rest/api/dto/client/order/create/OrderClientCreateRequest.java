package sk.tany.rest.api.dto.client.order.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderClientCreateRequest {
    @NotBlank
    private String cartId;
    private String note;
}
