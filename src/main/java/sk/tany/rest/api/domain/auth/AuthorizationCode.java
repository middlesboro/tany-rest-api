package sk.tany.rest.api.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.Date; // Service passes Date?

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationCode {

    private String id;
    private String code;
    private String email;
    private String jwt;
    private Instant expiration;
    private Instant createdDate;
    private Instant updateDate;

    // Custom constructor to match service usage: new AuthorizationCode(code, email, expirationDate)
    public AuthorizationCode(String code, String email, Date expiration) {
        this.code = code;
        this.email = email;
        if (expiration != null) {
            this.expiration = expiration.toInstant();
        }
    }
}
