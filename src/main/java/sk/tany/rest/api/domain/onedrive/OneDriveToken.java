package sk.tany.rest.api.domain.onedrive;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class OneDriveToken extends BaseEntity {
    private String refreshToken;
}
