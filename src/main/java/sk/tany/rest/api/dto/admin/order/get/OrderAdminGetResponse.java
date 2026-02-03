package sk.tany.rest.api.dto.admin.order.get;

import lombok.Data;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.OrderStatusHistoryDto;
import sk.tany.rest.api.dto.PriceBreakDown;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderAdminGetResponse {
    private String id;
    private String cartId;
    private Instant createDate;
    private BigDecimal finalPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
    private BigDecimal productsPrice;
    private List<OrderItemDto> items;
    private String carrierId;
    private String paymentId;
    private AddressDto deliveryAddress;
    private AddressDto invoiceAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String customerId;
    private String customerName;
    private String email;
    private String phone;
    private String note;
    private PriceBreakDown priceBreakDown;
    private String selectedPickupPointName;
    private List<OrderStatusHistoryDto> statusHistory;
    private String carrierOrderStateLink;
    private boolean authenticatedUser;
    private Instant cancelDate;
    private Long creditNoteIdentifier;
}
