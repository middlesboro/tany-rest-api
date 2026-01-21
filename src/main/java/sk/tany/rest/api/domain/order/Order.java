package sk.tany.rest.api.domain.order;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.dto.PriceBreakDown;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class Order {

    @Id
    private String id;
    private Instant createDate;
    private Instant updateDate;
    private Long orderIdentifier;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productsPrice;
    private BigDecimal discountPrice;
    private List<String> appliedDiscountCodes;
    private List<OrderItem> items;
    private String carrierId;
    private String paymentId;
    private String selectedPickupPointId;
    private String selectedPickupPointName;
    private Address deliveryAddress;
    private Address invoiceAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String customerId;
    private String email;
    private String phone;
    private String firstname;
    private String lastname;
    private String note;
    private PriceBreakDown priceBreakDown;
    private OrderStatus status = OrderStatus.CREATED;

}
