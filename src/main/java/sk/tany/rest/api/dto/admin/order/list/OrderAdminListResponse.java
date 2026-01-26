package sk.tany.rest.api.dto.admin.order.list;

import lombok.Data;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.OrderItemDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderAdminListResponse {
    private String id;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productsPrice;
    private List<OrderItemDto> items;
    private String carrierName;
    private String paymentName;
    private AddressDto deliveryAddress;
    private AddressDto invoiceAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String customerName;
    private String note;
    private OrderStatus status;
}
