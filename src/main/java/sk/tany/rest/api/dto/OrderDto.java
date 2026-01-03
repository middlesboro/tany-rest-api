package sk.tany.rest.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDto {
    private String id;
    private Long orderIdentifier;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productsPrice;
    private List<OrderItemDto> items;
    private String carrierId;
    private String paymentId;
    private sk.tany.rest.api.domain.carrier.CarrierType carrierType;
    private sk.tany.rest.api.domain.payment.PaymentType paymentType;
    private AddressDto deliveryAddress;
    private AddressDto invoiceAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String customerId;
}
