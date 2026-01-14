package sk.tany.rest.api.domain.filter;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "filter_parameters")
public class FilterParameter {

    @Id
    private String id;
    private String name;
    private FilterParameterType type;
    private List<String> filterParameterValueIds;
    private Boolean active;
}
