package sk.tany.rest.api.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "authorization_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationCode {

    @Id
    private String id;

    private String jwt;

    @Indexed(expireAfterSeconds = 30)
    private Date createdAt;
}
