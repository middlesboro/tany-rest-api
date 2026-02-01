package sk.tany.rest.api.domain.common;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Sequence implements BaseEntity {

    @Id
    private String id;
    private Long seq;

    @Override
    public void setCreatedDate(Instant date) { }
    @Override
    public Instant getCreatedDate() { return null; }
    @Override
    public void setLastModifiedDate(Instant date) { }
    @Override
    public Instant getLastModifiedDate() { return null; }
}
