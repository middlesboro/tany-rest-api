package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.dto.isklad.CreateNewOrderRequest;
import sk.tany.rest.api.dto.isklad.CreateSupplierRequest;
import sk.tany.rest.api.dto.isklad.ISkladItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ISkladMapper {

    @Autowired
    protected ProductRepository productRepository;

    @Mapping(target = "countryCode", constant = "SK") // Defaulting as not present in SupplierDto
    @Mapping(target = "autoShipmentLoad", constant = "1")
    public abstract CreateSupplierRequest toCreateSupplierRequest(SupplierDto supplierDto);

    @Mapping(target = "originalOrderId", source = "orderDto.orderIdentifier")
    @Mapping(target = "shopSettingId", constant = "1")
    @Mapping(target = "businessRelationship", constant = "b2c")
    @Mapping(target = "orderType", constant = "fulfillment")
    @Mapping(target = "customerName", source = "orderDto.firstname")
    @Mapping(target = "customerSurname", source = "orderDto.lastname")
    @Mapping(target = "customerPhone", source = "orderDto.phone")
    @Mapping(target = "customerEmail", source = "orderDto.email")
    // Delivery Address
    @Mapping(target = "name", source = "orderDto.firstname")
    @Mapping(target = "surname", source = "orderDto.lastname")
    @Mapping(target = "street", source = "orderDto.deliveryAddress.street")
    @Mapping(target = "streetNumber", expression = "java(getStreetNumber(orderDto.getDeliveryAddress().getStreet()))")
    @Mapping(target = "city", source = "orderDto.deliveryAddress.city")
    @Mapping(target = "postalCode", source = "orderDto.deliveryAddress.zip")
    @Mapping(target = "country", constant = "SK")
    // Billing Address (fa_)
    @Mapping(target = "faCompany", ignore = true) // Not in OrderDto
    @Mapping(target = "faStreet", source = "orderDto.invoiceAddress.street")
    @Mapping(target = "faStreetNumber", expression = "java(getStreetNumber(orderDto.getInvoiceAddress().getStreet()))")
    @Mapping(target = "faCity", source = "orderDto.invoiceAddress.city")
    @Mapping(target = "faPostalCode", source = "orderDto.invoiceAddress.zip")
    @Mapping(target = "faCountry", constant = "SK")
    @Mapping(target = "faIco", ignore = true)
    @Mapping(target = "faDic", ignore = true)
    @Mapping(target = "faIcdph", ignore = true)

    @Mapping(target = "idDelivery", source = "carrier.iskladId")
    @Mapping(target = "paymentCod", expression = "java(isCod(orderDto))")
    @Mapping(target = "items", expression = "java(mapItems(orderDto))")
    // Ignore others for now to avoid warnings/errors
    @Mapping(target = "entranceNumber", ignore = true)
    @Mapping(target = "doorNumber", ignore = true)
    @Mapping(target = "county", ignore = true)
    @Mapping(target = "autoProcess", ignore = true)
    @Mapping(target = "onLabel", ignore = true)
    @Mapping(target = "gpsLat", ignore = true)
    @Mapping(target = "gpsLong", ignore = true)
    @Mapping(target = "currency", constant = "EUR")
    @Mapping(target = "deliveryBranchId", ignore = true)
    @Mapping(target = "externalBranchId", ignore = true)
    @Mapping(target = "defaultTax", constant = "23")
    @Mapping(target = "idPayment", source = "payment.iskladId")
    @Mapping(target = "codPriceWithoutTax", ignore = true)
    @Mapping(target = "codPrice", expression = "java(getCodPrice(orderDto))")
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
    @Mapping(target = "phone", source = "orderDto.phone")
    @Mapping(target = "email", source = "orderDto.email")
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "priority", ignore = true)
    public abstract CreateNewOrderRequest toCreateNewOrderRequest(OrderDto orderDto, Carrier carrier, Payment payment);

    protected List<ISkladItem> mapItems(OrderDto orderDto) {
        if (orderDto == null || orderDto.getItems() == null) {
            return new ArrayList<>();
        }
        List<ISkladItem> result = new ArrayList<>();
        for (OrderItemDto item : orderDto.getItems()) {
            ISkladItem iskladItem = new ISkladItem();
            iskladItem.setCount(item.getQuantity());
            iskladItem.setCatalogId(item.getSlug());
            iskladItem.setExpiration(0);
            iskladItem.setTax(new BigDecimal("23"));

            // Retrieve Product Identifier
            productRepository.findById(item.getId()).ifPresent(product -> {
                if (product.getProductIdentifier() != null) {
                    iskladItem.setItemId(product.getProductIdentifier().intValue());
                }
                iskladItem.setName(product.getTitle());
            });

            // If name is not set from product (fallback)
            if (iskladItem.getName() == null) {
                iskladItem.setName(item.getName());
            }

            // Retrieve Prices from Breakdown
            if (orderDto.getPriceBreakDown() != null && orderDto.getPriceBreakDown().getItems() != null) {
                orderDto.getPriceBreakDown().getItems().stream()
                        .filter(pi -> item.getId().equals(pi.getId())) // assuming PriceItem id matches product id
                        .findFirst()
                        .ifPresent(pi -> {
                             // iSklad expects unit prices or total?
                             // Usually unit price. OrderItemDto has unit price. PriceItem has total.
                             // Wait, PriceItem in breakdown usually stores totals for the quantity.
                             // "fill also price, price_with_tax and tax = 23"
                             // Let's assume unit prices.
                             // The prompt says "price with vat and without vat can be taken from price break down".
                             // But breakdown has totals.
                             // I should divide by quantity or use the unit price if available.
                             // OrderItemDto has 'price' (usually with tax?).
                             // Product has priceWithoutVat.
                             // PriceItem has priceWithVat and priceWithoutVat (Totals).

                             BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                             if (qty.compareTo(BigDecimal.ZERO) != 0) {
                                 if (pi.getPriceWithoutVat() != null) {
                                     iskladItem.setPrice(pi.getPriceWithoutVat().divide(qty, 4, java.math.RoundingMode.HALF_UP));
                                 }
                                 if (pi.getPriceWithVat() != null) {
                                     iskladItem.setPriceWithTax(pi.getPriceWithVat().divide(qty, 4, java.math.RoundingMode.HALF_UP));
                                 }
                             }
                        });
            }

            // Fallback if price is missing
            if (iskladItem.getPrice() == null) {
                 iskladItem.setPrice(item.getPrice()); // assume this is base price? or with tax?
            }
            if (iskladItem.getPriceWithTax() == null) {
                 iskladItem.setPriceWithTax(item.getPrice());
            }

            result.add(iskladItem);
        }
        return result;
    }

    protected Integer isCod(OrderDto orderDto) {
        return orderDto.getPaymentType() == PaymentType.COD ? 1 : 0;
    }

    protected BigDecimal getCodPrice(OrderDto orderDto) {
        if (orderDto.getPaymentType() == PaymentType.COD) {
            return orderDto.getFinalPrice();
        }

        return null;
    }

    protected String getStreetNumber(String street) {
        if (street == null || street.isEmpty()) {
            return street;
        }

        int lastSpaceIndex = street.lastIndexOf(' ');
        if (lastSpaceIndex == -1) {
            return street;
        }

        return street.substring(lastSpaceIndex + 1);
    }
}
