package sk.tany.rest.api.dto.admin.shopsettings.list;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ShopSettingsListResponse {
    private String id;
    private String bankName;
    private String bankAccount;
    private String bankBic;
    private String shopStreet;
    private String shopZip;
    private String shopCity;
    private String shopPhoneNumber;
    private String shopEmail;
    private String organizationName;
    private String ico;
    private String dic;
    private String vatNumber;
    private BigDecimal vat;
    private Instant createdDate;
    private Instant updateDate;
}
