package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.domain.carrier.CarrierType;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderDto {
    private String id;
    private Long orderIdentifier;
    private Instant createDate;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productsPrice;
    private BigDecimal discountPrice;
    private List<String> appliedDiscountCodes;
    private List<OrderItemDto> items;
    private String carrierId;
    private String paymentId;
    private CarrierType carrierType;
    private PaymentType paymentType;
    private String carrierName;
    private String paymentName;
    private AddressDto deliveryAddress;
    private AddressDto invoiceAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String selectedPickupPointId;
    private String selectedPickupPointName;
    private String customerId;
    private OrderStatus status;
    private String email;
    private String phone;
    private String firstname;
    private String lastname;
    private String note;
    private List<String> cartDiscountIds;
    private PriceBreakDown priceBreakDown;
    private List<OrderStatusHistoryDto> statusHistory;
    private String carrierOrderStateLink;
    private boolean authenticatedUser;
    private Instant cancelDate;
    private Long creditNoteIdentifier;
}
