package sk.tany.rest.api.dto.client.order.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.domain.carrier.CarrierType;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.PaymentType;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderClientGetResponse {
    private String id;
    private String cartId;
    private BigDecimal finalPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productsPrice;
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
    private String customerId;
    private String email;
    private String phone;
    private String firstname;
    private String lastname;
    private OrderStatus status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String id;
        private String name;
        private Integer quantity;
        private BigDecimal price;
        private String image;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String street;
        private String city;
        private String zip;
    }
}
