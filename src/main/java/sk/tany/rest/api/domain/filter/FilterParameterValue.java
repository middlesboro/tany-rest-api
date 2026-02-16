package sk.tany.rest.api.domain.filter;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class FilterParameterValue implements BaseEntity {

    @Id
    private String id;
    private String name;
    private String filterParameterId;
    private Boolean active;

    @Override
    public void setCreatedDate(Instant date) { }
    @Override
    public Instant getCreatedDate() { return null; }
    @Override
    public void setLastModifiedDate(Instant date) { }
    @Override
    public Instant getLastModifiedDate() { return null; }
}
