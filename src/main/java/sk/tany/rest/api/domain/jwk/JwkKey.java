package sk.tany.rest.api.domain.jwk;

import lombok.Data;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.io.Serializable;
import java.time.Instant;

@Data
public class JwkKey implements Serializable, BaseEntity {
    @Id
    private String id;
    private String privateKey;
    private String publicKey;
    private String keyId;
    private Instant createdDate;

    @Override
    public void setLastModifiedDate(Instant date) { }
    @Override
    public Instant getLastModifiedDate() { return null; }
}
