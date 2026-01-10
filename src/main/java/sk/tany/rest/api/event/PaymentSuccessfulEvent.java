package sk.tany.rest.api.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentSuccessfulEvent extends ApplicationEvent {
    private final String orderId;

    public PaymentSuccessfulEvent(Object source, String orderId) {
        super(source);
        this.orderId = orderId;
    }
}
