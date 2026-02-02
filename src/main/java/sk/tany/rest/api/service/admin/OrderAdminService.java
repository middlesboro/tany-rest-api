package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.dto.OrderDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface OrderAdminService {
    Page<OrderDto> findAll(Long orderIdentifier, OrderStatus status, BigDecimal priceFrom, BigDecimal priceTo, String carrierId, String paymentId, Instant createDateFrom, Instant createDateTo, Pageable pageable);
    Optional<OrderDto> findById(String id);
    OrderDto save(OrderDto orderDto);
    OrderDto update(String id, OrderDto orderDto);
    OrderDto patch(String id, sk.tany.rest.api.dto.admin.order.patch.OrderPatchRequest patchDto);
    void deleteById(String id);
    void exportToIsklad(String orderId);
}
