package sk.tany.features.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class InvoiceDataDto {

    // Shop Details
    private String companyName;
    private String shopIco;
    private String shopDic;
    private String shopIcdph;
    private String shopBankAccount;
    private String shopBankName;
    private String shopIban;
    private String shopSwift;
    private String shopEmail;
    private String shopPhone;
    private String shopWebsite;

    private String shopStreet;
    private String shopCity;
    private String shopZip;
    private String shopCountry;

    // Order Details
    private Long orderIdentifier;
    private Instant createDate;
    private Instant paymentNotificationDate;
    private String email;
    private String phone;
    private String status;
    private String note;
    private String company;
    private String ico;
    private String dic;
    private String icdph;

    // Billing Address
    private String billingFirstname;
    private String billingLastname;
    private String billingStreet;
    private String billingCity;
    private String billingZip;
    private String billingCountry;

    // Delivery Address
    private String deliveryFirstname;
    private String deliveryLastname;
    private String deliveryStreet;
    private String deliveryCity;
    private String deliveryZip;
    private String deliveryCountry;

    // Financials
    private BigDecimal itemsPrice;
    private BigDecimal carrierPrice;
    private BigDecimal paymentPrice;
    private BigDecimal discountValue;
    private BigDecimal totalPrice;

    // Items
    private List<InvoiceItemDto> items;

    // Texts
    private String paymentName;
    private String carrierName;

    @Data
    public static class InvoiceItemDto {
        private String id;
        private String productId;
        private String variantId;
        private String title;
        private String variantTitle;
        private BigDecimal price;
        private Integer quantity;
        private String productCode;
    }
}
