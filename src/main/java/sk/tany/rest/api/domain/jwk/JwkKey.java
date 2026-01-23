package sk.tany.rest.api.domain.jwk;

import lombok.Data;
import org.dizitart.no2.objects.Id;

import java.io.Serializable;
import java.time.Instant;

@Data
public class JwkKey implements Serializable {
    @Id
    private String id;
    private String privateKey;
    private String publicKey;
    private String keyId;
    private Instant createdDate;
}
