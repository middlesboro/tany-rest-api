package sk.tany.rest.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDto {
    private String id;
    private String cartId;
    private BigDecimal finalPrice;
    private List<String> productIds;
    private String customerId;
}
