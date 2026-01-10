package sk.tany.rest.api.domain.order;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import sk.tany.rest.api.domain.customer.Address;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    @CreatedDate
    private Instant createDate;
    @LastModifiedDate
    private Instant updateDate;
    private Long orderIdentifier;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productsPrice;
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
    private OrderStatus status = OrderStatus.CREATED;

}
