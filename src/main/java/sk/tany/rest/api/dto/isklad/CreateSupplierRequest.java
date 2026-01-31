package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierRequest {
    private String name;

    @JsonProperty("auto_shipment_load")
    private Integer autoShipmentLoad;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("delivery_days")
    private String deliveryDays;

    @JsonProperty("vat_payer")
    private Integer vatPayer;

    private String street;

    @JsonProperty("street_number")
    private String streetNumber;

    @JsonProperty("postal_code")
    private String postalCode;

    private String city;

    private String ico;
    private String dic;
    @JsonProperty("ic_dph")
    private String icDph;

    @JsonProperty("email_orders")
    private String emailOrders;

    @JsonProperty("email_info")
    private String emailInfo;

    @JsonProperty("tax_rate")
    private BigDecimal taxRate;
}
