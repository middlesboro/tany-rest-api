package sk.tany.rest.api.dto.admin.order.create;

import lombok.Data;

@Data
public class OrderCreateItemDto {
    private String id;
    private String name;
    private Integer quantity;
}
