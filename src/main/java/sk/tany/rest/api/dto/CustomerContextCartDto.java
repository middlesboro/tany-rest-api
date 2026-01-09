package sk.tany.rest.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomerContextCartDto {

    private String cartId;
    private String customerId;
    private List<ProductDto> products;
    private java.math.BigDecimal totalProductPrice;
    private List<CarrierDto> carriers;
    private List<PaymentDto> payments;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;

}
