package sk.tany.rest.api.domain.jwk;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sk.tany.rest.api.domain.BaseEntity;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class JwkKey extends BaseEntity implements Serializable {
    private String privateKey;
    private String publicKey;
    private String keyId;
}
