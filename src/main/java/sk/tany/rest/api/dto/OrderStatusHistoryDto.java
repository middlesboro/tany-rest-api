package sk.tany.rest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.domain.order.OrderStatus;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryDto {
    private OrderStatus status;
    private Instant createdAt;
}
