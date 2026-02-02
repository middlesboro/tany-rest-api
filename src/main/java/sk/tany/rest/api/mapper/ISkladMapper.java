package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.dto.isklad.CreateNewOrderRequest;
import sk.tany.rest.api.dto.isklad.CreateSupplierRequest;
import sk.tany.rest.api.dto.isklad.ISkladItem;

@Mapper(componentModel = "spring")
public interface ISkladMapper {

    @Mapping(target = "countryCode", constant = "SK") // Defaulting as not present in SupplierDto
    @Mapping(target = "autoShipmentLoad", constant = "1")
    CreateSupplierRequest toCreateSupplierRequest(SupplierDto supplierDto);

    @Mapping(target = "originalOrderId", source = "orderIdentifier")
    @Mapping(target = "shopSettingId", constant = "1")
    @Mapping(target = "businessRelationship", constant = "b2c")
    @Mapping(target = "orderType", constant = "fulfillment")
    @Mapping(target = "customerName", source = "firstname")
    @Mapping(target = "customerSurname", source = "lastname")
    @Mapping(target = "customerPhone", source = "phone")
    @Mapping(target = "customerEmail", source = "email")
    // Delivery Address
    @Mapping(target = "name", source = "firstname")
    @Mapping(target = "surname", source = "lastname")
    @Mapping(target = "street", source = "deliveryAddress.street")
    @Mapping(target = "streetNumber", constant = ".") // Mandatory but missing in source
    @Mapping(target = "city", source = "deliveryAddress.city")
    @Mapping(target = "postalCode", source = "deliveryAddress.zip")
    @Mapping(target = "country", source = "deliveryAddress.country")
    // Billing Address (fa_)
    @Mapping(target = "faCompany", ignore = true) // Not in OrderDto
    @Mapping(target = "faStreet", source = "invoiceAddress.street")
    @Mapping(target = "faStreetNumber", constant = ".") // Mandatory but missing
    @Mapping(target = "faCity", source = "invoiceAddress.city")
    @Mapping(target = "faPostalCode", source = "invoiceAddress.zip")
    @Mapping(target = "faCountry", source = "invoiceAddress.country")
    @Mapping(target = "faIco", ignore = true)
    @Mapping(target = "faDic", ignore = true)
    @Mapping(target = "faIcdph", ignore = true)

    @Mapping(target = "idDelivery", constant = "1")
    @Mapping(target = "paymentCod", expression = "java(isCod(orderDto))")
    @Mapping(target = "items", source = "items")
    // Ignore others for now to avoid warnings/errors
    @Mapping(target = "entranceNumber", ignore = true)
    @Mapping(target = "doorNumber", ignore = true)
    @Mapping(target = "county", ignore = true)
    @Mapping(target = "autoProcess", ignore = true)
    @Mapping(target = "onLabel", ignore = true)
    @Mapping(target = "gpsLat", ignore = true)
    @Mapping(target = "gpsLong", ignore = true)
    @Mapping(target = "currency", constant = "EUR")
    @Mapping(target = "destinationCountryCode", source = "deliveryAddress.country")
    @Mapping(target = "deliveryBranchId", ignore = true)
    @Mapping(target = "externalBranchId", ignore = true)
    @Mapping(target = "defaultTax", constant = "20")
    @Mapping(target = "idPayment", constant = "1")
    @Mapping(target = "codPriceWithoutTax", ignore = true)
    @Mapping(target = "codPrice", ignore = true)
    @Mapping(target = "declaredValue", ignore = true)
    @Mapping(target = "depositWithoutTax", ignore = true)
    @Mapping(target = "deposit", ignore = true)
    @Mapping(target = "deliveryPriceWithoutTax", ignore = true)
    @Mapping(target = "paymentPriceWithoutTax", ignore = true)
    @Mapping(target = "discountPriceWithoutTax", ignore = true)
    @Mapping(target = "minDeliveryDate", ignore = true)
    @Mapping(target = "forcedCompletion", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "invoiceUrl", ignore = true)
    @Mapping(target = "faPrint", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    @Mapping(target = "myorderIdentifier", ignore = true)
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "priority", ignore = true)
    CreateNewOrderRequest toCreateNewOrderRequest(OrderDto orderDto);

    @Mapping(target = "itemId", ignore = true) // integer vs string id issue potentially
    @Mapping(target = "catalogId", source = "slug")
    @Mapping(target = "count", source = "quantity")
    @Mapping(target = "price", source = "price") // Assuming price is base, mapping to price (ex vat? uncertain)
    @Mapping(target = "priceWithTax", source = "price")
    @Mapping(target = "expiration", constant = "0")
    @Mapping(target = "expValue", ignore = true)
    @Mapping(target = "tax", constant = "20")
    ISkladItem toISkladItem(OrderItemDto item);

    default Integer isCod(OrderDto orderDto) {
        return 0;
    }
}
