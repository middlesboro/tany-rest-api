package sk.tany.rest.api.domain.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MagicLinkToken extends BaseEntity {
    private String jti;
    private String customerEmail;
    private MagicLinkTokenState state;
    private String jwt;
    private Instant expiration;
}
