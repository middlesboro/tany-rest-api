package sk.tany.rest.api.service.client.payment;

import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.payment.PaymentType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentTypeServiceFactory {

    private final Map<PaymentType, PaymentTypeService> serviceMap;

    public PaymentTypeServiceFactory(List<PaymentTypeService> services) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(PaymentTypeService::getSupportedType, Function.identity()));
    }

    public Optional<PaymentTypeService> getService(PaymentType type) {
        return Optional.ofNullable(serviceMap.get(type));
    }
}
