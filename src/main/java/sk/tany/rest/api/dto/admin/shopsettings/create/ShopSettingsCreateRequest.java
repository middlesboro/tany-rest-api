package sk.tany.rest.api.dto.admin.shopsettings.create;

import lombok.Data;

@Data
public class ShopSettingsCreateRequest {
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
}
