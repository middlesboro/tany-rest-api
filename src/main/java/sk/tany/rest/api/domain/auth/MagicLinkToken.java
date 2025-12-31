package sk.tany.rest.api.domain.auth;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "magic_link_tokens")
public class MagicLinkToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String jti;

    private String customerEmail;

    private MagicLinkTokenState state;

    @Indexed(expireAfterSeconds = 300)
    private Date createdAt;
}
