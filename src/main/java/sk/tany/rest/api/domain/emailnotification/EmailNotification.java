package sk.tany.rest.api.domain.emailnotification;

import lombok.Data;
import org.dizitart.no2.objects.Id;

import java.time.Instant;

@Data
public class EmailNotification {

    @Id
    private String id;
    private String email;
    private String productId;
    private Instant createDate;

}
