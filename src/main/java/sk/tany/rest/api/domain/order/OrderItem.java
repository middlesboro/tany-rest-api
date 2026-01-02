package sk.tany.rest.api.domain.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String id;
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private String image;
}
