package sk.tany.rest.api.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SupplierDto {
    private String id;
    private String name;
    private Instant createdDate;
    private Instant updateDate;
}
