package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewOrderRequest {
    @JsonProperty("original_order_id")
    private String originalOrderId;

    @JsonProperty("shop_setting_id")
    private Integer shopSettingId;

    @JsonProperty("business_relationship")
    private String businessRelationship; // b2b, b2c

    @JsonProperty("order_type")
    private String orderType; // fulfillment, shipping_label, virtual_order

    private Integer priority;

    @JsonProperty("reference_number")
    private String referenceNumber;

    @JsonProperty("myorder_identifier")
    private String myorderIdentifier;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_surname")
    private String customerSurname;

    @JsonProperty("customer_phone")
    private String customerPhone;

    @JsonProperty("customer_email")
    private String customerEmail;

    private String name;
    private String surname;
    private String phone;
    private String email;
    private String company;
    private String street;
    @JsonProperty("street_number")
    private String streetNumber;
    @JsonProperty("entrance_number")
    private String entranceNumber;
    @JsonProperty("door_number")
    private String doorNumber;
    private String city;
    private String county;
    private String country;
    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("fa_company")
    private String faCompany;
    @JsonProperty("fa_street")
    private String faStreet;
    @JsonProperty("fa_street_number")
    private String faStreetNumber;
    @JsonProperty("fa_city")
    private String faCity;
    @JsonProperty("fa_country")
    private String faCountry;
    @JsonProperty("fa_postal_code")
    private String faPostalCode;
    @JsonProperty("fa_ico")
    private String faIco;
    @JsonProperty("fa_dic")
    private String faDic;
    @JsonProperty("fa_icdph")
    private String faIcdph;

    @JsonProperty("auto_process")
    private Integer autoProcess;

    @JsonProperty("on_label")
    private String onLabel;

    @JsonProperty("gps_lat")
    private String gpsLat;
    @JsonProperty("gps_long")
    private String gpsLong;

    private String note;
    private String currency;

    @JsonProperty("destination_country_code")
    private String destinationCountryCode;

    @JsonProperty("id_delivery")
    private Integer idDelivery;

    @JsonProperty("delivery_branch_id")
    private Integer deliveryBranchId;

    @JsonProperty("external_branch_id")
    private String externalBranchId;

    @JsonProperty("default_tax")
    private BigDecimal defaultTax;

    @JsonProperty("id_payment")
    private Integer idPayment;

    @JsonProperty("payment_cod")
    private Integer paymentCod;

    @JsonProperty("cod_price_without_tax")
    private BigDecimal codPriceWithoutTax;

    @JsonProperty("cod_price")
    private BigDecimal codPrice;

    @JsonProperty("declared_value")
    private BigDecimal declaredValue;

    @JsonProperty("deposit_without_tax")
    private BigDecimal depositWithoutTax;
    private BigDecimal deposit;

    @JsonProperty("delivery_price_without_tax")
    private BigDecimal deliveryPriceWithoutTax;
    @JsonProperty("delivery_price")
    private BigDecimal deliveryPrice;

    @JsonProperty("payment_price_without_tax")
    private BigDecimal paymentPriceWithoutTax;
    @JsonProperty("payment_price")
    private BigDecimal paymentPrice;

    @JsonProperty("discount_price_without_tax")
    private BigDecimal discountPriceWithoutTax;
    @JsonProperty("discount_price")
    private BigDecimal discountPrice;

    @JsonProperty("min_delivery_date")
    private String minDeliveryDate;

    @JsonProperty("forced_completion")
    private Boolean forcedCompletion;

    private List<ISkladItem> items;

    private ISkladInvoice invoice;

    @JsonProperty("invoice_url")
    private String invoiceUrl;

    @JsonProperty("fa_print")
    private Integer faPrint;

    private List<String> attachments;

    // Packages skipped for now as per docs it's for shipping_label type
}
