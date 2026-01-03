package sk.tany.rest.api.domain.shopsettings;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "shop_settings")
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
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant updateDate;
}
