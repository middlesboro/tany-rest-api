package sk.tany.rest.api.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkToken {

    @Id
    private String id;
    private String jti;
    private String customerEmail;
    private MagicLinkTokenState state;
    private String jwt;
    private Instant expiration;
    private Instant createdDate;
    private Instant updateDate;
}
