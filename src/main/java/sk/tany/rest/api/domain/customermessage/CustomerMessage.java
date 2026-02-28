package sk.tany.rest.api.domain.customermessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import sk.tany.rest.api.domain.BaseEntity;

@Document
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMessage extends BaseEntity {

    private String message;
    private String email;
    private MessageType type;

}
