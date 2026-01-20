package sk.tany.rest.api.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkToken {

    private String id;
    private String jti;
    private String customerEmail;
    private MagicLinkTokenState state;
    private String jwt; // Assuming this is needed based on "getJwt" error in AuthCode (maybe mixed up?) or similar logic.
    private Instant expiration;
    private Instant createdDate;
    private Instant updateDate;
}
