package sk.tany.rest.api.dto.client.order.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.dto.AddressDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderClientCreateResponse {
    private String id;
    private String cartId;
    private Instant createDate;
    private Long orderIdentifier;
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
    private boolean deliveryAddressSameAsInvoiceAddress;
    private String customerId;

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
}
