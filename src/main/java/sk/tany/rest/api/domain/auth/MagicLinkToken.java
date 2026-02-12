package sk.tany.rest.api.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkToken implements BaseEntity {

    @Id
    private String id;
    private String jti;
    private String customerEmail;
    private MagicLinkTokenState state;
    private String jwt;
    private Instant expiration;
    private Instant createdDate;
    private Instant updateDate;

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
