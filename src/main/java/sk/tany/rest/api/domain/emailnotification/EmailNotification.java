package sk.tany.rest.api.domain.emailnotification;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class EmailNotification extends BaseEntity {
    private String email;
    private String productId;
}
