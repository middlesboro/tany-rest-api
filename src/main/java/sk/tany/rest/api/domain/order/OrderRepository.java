package sk.tany.rest.api.domain.order;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OrderRepository extends AbstractInMemoryRepository<Order> {

    public OrderRepository(Nitrite nitrite) {
        super(nitrite, Order.class);
    }

    public List<Order> findAllByCustomerId(String customerId) {
        return memoryCache.values().stream()
                .filter(o -> o.getCustomerId() != null && o.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }
}
