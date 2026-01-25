package sk.tany.rest.api.domain.order;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
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

    public Page<Order> findAllByCustomerId(String customerId, Pageable pageable) {
        List<Order> all = memoryCache.values().stream()
                .filter(o -> o.getCustomerId() != null && o.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(Order::getCreateDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        if (start > all.size()) {
            return new PageImpl<>(List.of(), pageable, all.size());
        }
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }
}
