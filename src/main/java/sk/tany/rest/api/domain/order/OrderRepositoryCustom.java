package sk.tany.rest.api.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderRepositoryCustom {
    Page<Order> findAll(Long orderIdentifier, OrderStatus status, BigDecimal priceFrom, BigDecimal priceTo, String carrierId, String paymentId, Instant createDateFrom, Instant createDateTo, Pageable pageable);
}
