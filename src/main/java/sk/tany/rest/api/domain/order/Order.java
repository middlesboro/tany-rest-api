package sk.tany.rest.api.domain.order;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import sk.tany.rest.api.domain.BaseEntity;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.dto.PriceBreakDown;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class Order implements BaseEntity {

    @Id
    private String id;
    private Instant createDate;
    private Instant updateDate;
    @Indexed(unique = true)
    private Long orderIdentifier;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
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
    private List<OrderStatusHistory> statusHistory = new java.util.ArrayList<>();
    private String carrierOrderStateLink;
    private boolean authenticatedUser;
    private Instant cancelDate;
    private Long creditNoteIdentifier;
    private boolean invoiceUploadedToOneDrive;
    private boolean creditNoteUploadedToOneDrive;
    private Instant iskladImportDate;
    private Instant adminNotificationDate;

    @Override
    public void setCreatedDate(Instant date) {
        this.createDate = date;
    }
    @Override
    public Instant getCreatedDate() {
        return this.createDate;
    }
    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }

    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "orderIdentifier": return orderIdentifier;
            case "finalPrice": return finalPrice;
            case "status": return status;
            case "email": return email;
            case "lastname": return lastname;
            default: return BaseEntity.super.getSortValue(field);
        }
    }
}
