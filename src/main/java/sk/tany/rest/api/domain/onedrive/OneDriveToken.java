package sk.tany.rest.api.domain.onedrive;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
public class OneDriveToken {
    @Id
    private String id;
    private String refreshToken;
    private Instant updateDate;
}
