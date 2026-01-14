package sk.tany.rest.api.domain.filter;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "filter_parameter_values")
public class FilterParameterValue {

    @Id
    private String id;
    private String name;
    private Boolean active;
}
