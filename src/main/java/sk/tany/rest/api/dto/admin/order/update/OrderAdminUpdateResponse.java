package sk.tany.rest.api.dto.admin.order.update;

import lombok.Data;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.OrderItemDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderAdminUpdateResponse {
    private String id;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal productsPrice;
    private List<OrderItemDto> items;
    private String carrierId;
    private String paymentId;
    private AddressDto deliveryAddress;
    private AddressDto invoiceAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String customerId;
    private String note;
}
