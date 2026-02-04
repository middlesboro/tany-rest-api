package sk.tany.rest.api.domain.order;

import org.dizitart.no2.Nitrite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class OrderRepository extends AbstractInMemoryRepository<Order> {

    public OrderRepository(Nitrite nitrite) {
        super(nitrite, Order.class);
    }

    public java.util.Optional<Order> findByOrderIdentifier(Long orderIdentifier) {
        return memoryCache.values().stream()
                .filter(o -> o.getOrderIdentifier() != null && o.getOrderIdentifier().equals(orderIdentifier))
                .findFirst();
    }

    public java.util.Optional<Order> findByCartId(String cartId) {
        return memoryCache.values().stream()
                .filter(o -> o.getCartId() != null && o.getCartId().equals(cartId))
                .findFirst();
    }

    public List<Order> findAllByCustomerId(String customerId) {
        return memoryCache.values().stream()
                .filter(o -> o.getCustomerId() != null && o.getCustomerId().equals(customerId))
                .toList();
    }

    public Page<Order> findAllByCustomerId(String customerId, Pageable pageable) {
        List<Order> all = memoryCache.values().stream()
                .filter(o -> o.getCustomerId() != null && o.getCustomerId().equals(customerId))
                .collect(Collectors.toCollection(ArrayList::new));

        if (pageable.getSort().isSorted()) {
            sort(all, pageable.getSort());
        } else {
            all.sort(Comparator.comparing(Order::getCreateDate, Comparator.nullsLast(Comparator.reverseOrder())));
        }

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

    public Page<Order> findAllByCustomerIdAndAuthenticatedUserTrue(String customerId, Pageable pageable) {
        List<Order> all = memoryCache.values().stream()
                .filter(o -> o.getCustomerId() != null && o.getCustomerId().equals(customerId))
                .filter(Order::isAuthenticatedUser)
                .collect(Collectors.toCollection(ArrayList::new));

        if (pageable.getSort().isSorted()) {
            sort(all, pageable.getSort());
        } else {
            all.sort(Comparator.comparing(Order::getCreateDate, Comparator.nullsLast(Comparator.reverseOrder())));
        }

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

    public Page<Order> findAll(Long orderIdentifier, OrderStatus status, BigDecimal priceFrom, BigDecimal priceTo, String carrierId, String paymentId, Instant createDateFrom, Instant createDateTo, Pageable pageable) {
        Stream<Order> stream = memoryCache.values().stream();

        if (orderIdentifier != null) {
            stream = stream.filter(o -> o.getOrderIdentifier() != null && o.getOrderIdentifier().equals(orderIdentifier));
        }
        if (status != null) {
            stream = stream.filter(o -> o.getStatus() == status);
        }
        if (priceFrom != null) {
            stream = stream.filter(o -> o.getFinalPrice() != null && o.getFinalPrice().compareTo(priceFrom) >= 0);
        }
        if (priceTo != null) {
            stream = stream.filter(o -> o.getFinalPrice() != null && o.getFinalPrice().compareTo(priceTo) <= 0);
        }
        if (carrierId != null) {
            stream = stream.filter(o -> o.getCarrierId() != null && o.getCarrierId().equals(carrierId));
        }
        if (paymentId != null) {
            stream = stream.filter(o -> o.getPaymentId() != null && o.getPaymentId().equals(paymentId));
        }
        if (createDateFrom != null) {
            stream = stream.filter(o -> o.getCreateDate() != null && !o.getCreateDate().isBefore(createDateFrom));
        }
        if (createDateTo != null) {
            stream = stream.filter(o -> o.getCreateDate() != null && !o.getCreateDate().isAfter(createDateTo));
        }

        List<Order> all = stream.collect(Collectors.toCollection(ArrayList::new));

        if (pageable.getSort().isSorted()) {
            List<Sort.Order> orders = new ArrayList<>();
            for (Sort.Order order : pageable.getSort()) {
                if ("price".equals(order.getProperty())) {
                    orders.add(new Sort.Order(order.getDirection(), "finalPrice", order.getNullHandling()));
                } else {
                    orders.add(order);
                }
            }
            sort(all, Sort.by(orders));
        } else {
            all.sort(Comparator.comparing(Order::getCreateDate, Comparator.nullsLast(Comparator.reverseOrder())));
        }

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

    public List<Order> findByInvoiceUploadedToOneDriveFalse() {
        return memoryCache.values().stream()
                .filter(o -> !o.isInvoiceUploadedToOneDrive())
                .collect(Collectors.toList());
    }

    public List<Order> findByCreditNoteUploadedToOneDriveFalse() {
        return memoryCache.values().stream()
                .filter(o -> !o.isCreditNoteUploadedToOneDrive())
                .collect(Collectors.toList());
    }

    public List<Order> findAllByIskladImportDateIsNullAndStatusNot(OrderStatus status) {
        return memoryCache.values().stream()
                .filter(o -> o.getIskladImportDate() == null)
                .filter(o -> o.getStatus() != status)
                .collect(Collectors.toList());
    }
}
