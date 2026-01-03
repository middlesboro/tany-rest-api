package sk.tany.rest.api.domain.common;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "sequences")
public class Sequence {
    @Id
    private String id;
    private long seq;
}
