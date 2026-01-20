package sk.tany.rest.api.domain.common;

import lombok.Data;
import org.dizitart.no2.objects.Id;

@Data
public class Sequence {

    @Id
    private String id;
    private Long seq;
}
