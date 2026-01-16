package sk.tany.rest.api.dto.admin.order.patch;

import lombok.Data;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.OrderItemDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderPatchRequest {
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productsPrice;
    private List<OrderItemDto> items;
    private String carrierId;
    private String paymentId;
    private AddressDto deliveryAddress;
    private AddressDto invoiceAddress;
    private Boolean deliveryAddressSameAsInvoiceAddress;
    private String customerId;
    private String note;
}
