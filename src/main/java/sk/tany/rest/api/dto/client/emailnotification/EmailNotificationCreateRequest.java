package sk.tany.rest.api.dto.client.emailnotification;

import lombok.Data;

@Data
public class EmailNotificationCreateRequest {
    private String email;
    private String productId;
}
