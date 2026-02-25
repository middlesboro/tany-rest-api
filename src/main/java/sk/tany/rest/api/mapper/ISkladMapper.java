package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
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
    @Mapping(target = "street", source = "orderDto.deliveryAddress.street", qualifiedByName = "extractStreet")
    @Mapping(target = "streetNumber", source = "orderDto.deliveryAddress.street", qualifiedByName = "extractStreetNumber")
    @Mapping(target = "city", source = "orderDto.deliveryAddress.city")
    @Mapping(target = "postalCode", source = "orderDto.deliveryAddress.zip")
    @Mapping(target = "country", constant = "SK")
    // Billing Address (fa_)
    @Mapping(target = "faCompany", ignore = true) // Not in OrderDto
    @Mapping(target = "faStreet", source = "orderDto.invoiceAddress.street", qualifiedByName = "extractStreet")
    @Mapping(target = "faStreetNumber", source = "orderDto.invoiceAddress.street", qualifiedByName = "extractStreetNumber")
    @Mapping(target = "faCity", source = "orderDto.invoiceAddress.city")
    @Mapping(target = "faPostalCode", source = "orderDto.invoiceAddress.zip")
    @Mapping(target = "faCountry", constant = "SK")
    @Mapping(target = "faIco", ignore = true)
    @Mapping(target = "faDic", ignore = true)
    @Mapping(target = "faIcdph", ignore = true)

    @Mapping(target = "idDelivery", source = "carrier.iskladId")
    @Mapping(target = "paymentCod", expression = "java(isCod(payment))")
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
    @Mapping(target = "externalBranchId", source = "orderDto.selectedPickupPointId")
    @Mapping(target = "defaultTax", constant = "23")
    @Mapping(target = "idPayment", source = "payment.iskladId")
    @Mapping(target = "codPriceWithoutTax", ignore = true)
    @Mapping(target = "codPrice", expression = "java(getCodPrice(orderDto, payment))")
    @Mapping(target = "declaredValue", ignore = true)
    @Mapping(target = "depositWithoutTax", ignore = true)
    @Mapping(target = "deposit", ignore = true)
    @Mapping(target = "deliveryPrice", source = "orderDto", qualifiedByName = "extractDeliveryPrice")
    @Mapping(target = "deliveryPriceWithoutTax", source = "orderDto", qualifiedByName = "extractDeliveryPriceWithoutTax")
    @Mapping(target = "paymentPrice", source = "orderDto", qualifiedByName = "extractPaymentPrice")
    @Mapping(target = "paymentPriceWithoutTax", source = "orderDto", qualifiedByName = "extractPaymentPriceWithoutTax")
    @Mapping(target = "discountPrice", source = "orderDto", qualifiedByName = "extractDiscountPrice")
    @Mapping(target = "discountPriceWithoutTax", source = "orderDto", qualifiedByName = "extractDiscountPriceWithoutTax")
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
            iskladItem.setExpiration(0);
            iskladItem.setTax(new BigDecimal("23"));

            // Retrieve Product Identifier
            productRepository.findById(item.getId()).ifPresent(product -> {
                if (product.getProductIdentifier() != null) {
                    String input = String.valueOf(product.getProductIdentifier());
                    int inputLength = input.length();
                    int targetLength = (inputLength <= 3) ? 10 : 11;
                    iskladItem.setItemId(Long.valueOf(String.format("%-" + targetLength + "s", input).replace(' ', '0')));
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
                             BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                             if (qty.compareTo(BigDecimal.ZERO) != 0) {
                                 if (pi.getPriceWithoutVat() != null) {
                                     iskladItem.setPrice(pi.getPriceWithoutVat().divide(qty, 2, java.math.RoundingMode.HALF_UP));
                                 }
                                 if (pi.getPriceWithVat() != null) {
                                     iskladItem.setPriceWithTax(pi.getPriceWithVat().divide(qty, 2, java.math.RoundingMode.HALF_UP));
                                 }
                             }
                        });
            }

            // Fallback if price is missing
            if (iskladItem.getPrice() == null) {
                 iskladItem.setPrice(item.getPrice());
            }
            if (iskladItem.getPriceWithTax() == null) {
                 iskladItem.setPriceWithTax(item.getPrice());
            }

            result.add(iskladItem);
        }
        return result;
    }

    protected Integer isCod(Payment payment) {
        return payment.getType() == PaymentType.COD ? 1 : 0;
    }

    protected BigDecimal getCodPrice(OrderDto orderDto, Payment payment) {
        if (PaymentType.COD == payment.getType()) {
            return orderDto.getFinalPrice();
        }

        return null;
    }

    @Named("extractStreetNumber")
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

    @Named("extractStreet")
    protected String getStreet(String street) {
        if (street == null || street.isEmpty()) {
            return street;
        }

        int lastSpaceIndex = street.lastIndexOf(' ');
        if (lastSpaceIndex == -1) {
            return street;
        }

        return street.substring(0, lastSpaceIndex);
    }

    @Named("extractDeliveryPrice")
    protected BigDecimal getDeliveryPrice(OrderDto order) {
        for (PriceItem item : order.getPriceBreakDown().getItems()) {
            if (item.getType() == PriceItemType.CARRIER) {
                return item.getPriceWithVat();
            }
        }

        return null;
    }

    @Named("extractDeliveryPriceWithoutTax")
    protected BigDecimal getDeliveryPriceWithoutTax(OrderDto order) {
        for (PriceItem item : order.getPriceBreakDown().getItems()) {
            if (item.getType() == PriceItemType.CARRIER) {
                return item.getPriceWithoutVat();
            }
        }

        return null;
    }

    @Named("extractPaymentPrice")
    protected BigDecimal getPaymentPrice(OrderDto order) {
        for (PriceItem item : order.getPriceBreakDown().getItems()) {
            if (item.getType() == PriceItemType.PAYMENT) {
                return item.getPriceWithVat();
            }
        }

        return null;
    }

    @Named("extractPaymentPriceWithoutTax")
    protected BigDecimal getPaymentPriceWithoutTax(OrderDto order) {
        for (PriceItem item : order.getPriceBreakDown().getItems()) {
            if (item.getType() == PriceItemType.PAYMENT) {
                return item.getPriceWithoutVat();
            }
        }

        return null;
    }

    @Named("extractDiscountPrice")
    protected BigDecimal getDiscountPrice(OrderDto order) {
        BigDecimal finalDiscount = BigDecimal.ZERO;

        for (PriceItem item : order.getPriceBreakDown().getItems()) {
            if (item.getType() == PriceItemType.DISCOUNT) {
                finalDiscount = finalDiscount.add(item.getPriceWithVat());
            }
        }

        return finalDiscount;
    }

    @Named("extractDiscountPriceWithoutTax")
    protected BigDecimal getDiscountPriceWithoutTax(OrderDto order) {
        BigDecimal finalDiscount = BigDecimal.ZERO;

        for (PriceItem item : order.getPriceBreakDown().getItems()) {
            if (item.getType() == PriceItemType.DISCOUNT) {
                finalDiscount = finalDiscount.add(item.getPriceWithoutVat());
            }
        }

        return finalDiscount;
    }

}
