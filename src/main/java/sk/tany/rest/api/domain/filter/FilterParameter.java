package sk.tany.rest.api.domain.filter;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;
import java.util.List;

@Data
public class FilterParameter extends BaseEntity {
    private String name;
    private FilterParameterType type;
    private List<String> filterParameterValueIds;
    private Boolean active;
}
