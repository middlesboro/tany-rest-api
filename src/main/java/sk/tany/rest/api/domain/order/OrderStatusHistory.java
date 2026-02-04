package sk.tany.rest.api.domain.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    private OrderStatus status;
    private Instant createdAt;
    private Boolean emailSent;

    public OrderStatusHistory(OrderStatus status, Instant createdAt) {
        this.status = status;
        this.createdAt = createdAt;
    }
}
