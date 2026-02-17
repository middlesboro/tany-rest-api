package sk.tany.rest.api.domain.filter;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class FilterParameterValue extends BaseEntity {
    private String name;
    private String filterParameterId;
    private Boolean active;
}
