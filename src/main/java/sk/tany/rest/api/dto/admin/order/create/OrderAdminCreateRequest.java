package sk.tany.rest.api.dto.admin.order.create;

import lombok.Data;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.dto.AddressDto;

import java.util.List;

@Data
public class OrderAdminCreateRequest {
    private String carrierId;
    private String selectedPickupPointId;
    private String paymentId;
    private AddressDto deliveryAddress;
    private AddressDto invoiceAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String customerId;
    private String email;
    private String phone;
    private String firstname;
    private String lastname;
    private String note;
    private OrderStatus status;
    private List<OrderCreateItemDto> items;
    private List<String> cartDiscountIds;
}
