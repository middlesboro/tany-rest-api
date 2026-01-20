package sk.tany.rest.api.domain.filter;

import lombok.Data;
import org.dizitart.no2.objects.Id;

@Data
public class FilterParameterValue {

    @Id
    private String id;
    private String name;
    private String filterParameterId;
    private Boolean active;
}
