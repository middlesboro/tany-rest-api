package sk.tany.rest.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sk.tany.rest.api.domain.order.Order;

@Getter
@RequiredArgsConstructor
public class OrderStatusChangedEvent {
    private final Order order;
}
