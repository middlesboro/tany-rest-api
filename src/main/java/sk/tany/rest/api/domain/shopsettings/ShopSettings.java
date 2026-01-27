package sk.tany.rest.api.domain.shopsettings;

import lombok.Data;
import org.dizitart.no2.objects.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ShopSettings {

    @Id
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
    private String defaultCountry;
    private Instant createdDate;
    private Instant updateDate;
}
