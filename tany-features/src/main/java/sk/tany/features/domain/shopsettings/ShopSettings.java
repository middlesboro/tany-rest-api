package sk.tany.features.domain.shopsettings;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.features.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ShopSettings extends BaseEntity {
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
    private String defaultCountry;
}
