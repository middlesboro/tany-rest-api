package sk.tany.rest.api.dto.client.emailnotification;

import lombok.Data;
import sk.tany.rest.api.validation.client.customer.EmailNotificationCreateConstraint;

@Data
@EmailNotificationCreateConstraint
public class EmailNotificationCreateRequest {
    private String email;
    private String productId;
}
