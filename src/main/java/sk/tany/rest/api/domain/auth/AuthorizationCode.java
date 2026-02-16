package sk.tany.rest.api.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationCode extends BaseEntity {
private String code;
    private String email;
    private String jwt;
    private Instant expiration;
    private Instant createdDate;
public AuthorizationCode(String code, String email, Date expiration) {
        this.code = code;
        this.email = email;
        if (expiration != null) {
            this.expiration = expiration.toInstant();
        }
    }
}
