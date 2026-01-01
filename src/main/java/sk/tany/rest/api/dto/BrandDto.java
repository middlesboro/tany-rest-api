package sk.tany.rest.api.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class BrandDto {
    private String id;
    private String name;
    private String image;
    private Instant createdDate;
    private Instant updateDate;
}
