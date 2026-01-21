package sk.tany.rest.api.domain.filter;

import lombok.Data;
import org.dizitart.no2.objects.Id;

import java.util.List;

@Data
public class FilterParameter {

    @Id
    private String id;
    private String name;
    private FilterParameterType type;
    private List<String> filterParameterValueIds;
    private Boolean active;
}
