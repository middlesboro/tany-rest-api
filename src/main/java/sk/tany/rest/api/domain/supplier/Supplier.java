package sk.tany.rest.api.domain.supplier;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Supplier extends BaseEntity {
    private Long prestashopId;
    private String name;
    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "name": return name;
            default: return super.getSortValue(field);
        }
    }
}
