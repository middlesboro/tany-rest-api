package sk.tany.rest.api.dto.admin.import_email_notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailNotificationImportDataDto {

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("id_product")
    private String idProduct;
}
