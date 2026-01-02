package sk.tany.rest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private String id;
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private String image;
}
