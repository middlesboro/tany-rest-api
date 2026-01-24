package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.CustomerContextCartDto;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientDetailResponse;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateRequest;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateResponse;

@Mapper(componentModel = "spring")
public interface CustomerClientApiMapper {

    CustomerDto toDto(CustomerClientUpdateRequest request);

    CustomerClientUpdateResponse toUpdateResponse(CustomerDto customerDto);

    @Mapping(target = "customerDto", source = "customerDto")
    @Mapping(target = "cartDto", source = "cartDto")
    CustomerClientGetResponse toGetResponse(CustomerContextDto customerContextDto);

    // Helpers for CartDiscountClientDto
    sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse.CartDiscountClientDto toGetCartDiscountDto(sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto cartDiscountDto);

    // Helpers for AddressDto
    AddressDto toAddressDto(CustomerClientUpdateRequest.AddressDto addressDto);
    CustomerClientUpdateResponse.AddressDto toUpdateAddress(AddressDto addressDto);
    CustomerClientGetResponse.AddressDto toGetAddress(AddressDto addressDto);

    // Helpers for Nested CustomerClientGetResponse mapping
    CustomerClientGetResponse.CustomerDto toGetCustomerDto(CustomerDto customerDto);
    CustomerClientGetResponse.CustomerContextCartDto toGetCartDto(CustomerContextCartDto cartDto);
    CustomerClientGetResponse.ProductDto toGetProductDto(ProductClientDto productDto);
    CustomerClientGetResponse.CarrierDto toGetCarrierDto(CarrierDto carrierDto);
    CustomerClientGetResponse.PaymentDto toGetPaymentDto(PaymentDto paymentDto);

    CustomerClientDetailResponse toDetailResponse(CustomerDto customerDto);
}
