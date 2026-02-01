package sk.tany.rest.api.domain.supplier;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Supplier implements BaseEntity {

    @Id
    private String id;
    private Long prestashopId;
    private String name;
    private Instant createdDate;
    private Instant updateDate;

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }

    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
