package sk.tany.rest.api.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationCode implements BaseEntity {

    @Id
    private String id;
    private String code;
    private String email;
    private String jwt;
    private Instant expiration;
    private Instant createdDate;
    private Instant updateDate;

    public AuthorizationCode(String code, String email, Date expiration) {
        this.code = code;
        this.email = email;
        if (expiration != null) {
            this.expiration = expiration.toInstant();
        }
    }

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
