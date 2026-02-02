package sk.tany.rest.api.dto.admin.order.list;

import lombok.Data;
import sk.tany.rest.api.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class OrderAdminListResponse {
    private String id;
    private String orderIdentifier;
    private String cartId;
    private BigDecimal finalPrice;
    private String carrierName;
    private String paymentName;
    private String customerName;
    private OrderStatus status;
    private Instant createDate;
    private String carrierOrderStateLink;
    private Instant iskladImportDate;
}
