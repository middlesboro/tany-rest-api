package sk.tany.rest.api.domain.supplier;

import lombok.Data;
import org.dizitart.no2.repository.annotations.Id;
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
    public Object getSortValue(String field) {
        switch (field) {
            case "name": return name;
            default: return BaseEntity.super.getSortValue(field);
        }
    }

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }

    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
