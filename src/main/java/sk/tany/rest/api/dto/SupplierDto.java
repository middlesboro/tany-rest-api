package sk.tany.rest.api.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class SupplierDto {
    private String id;
    private String name;
    private String metaTitle;
    private String metaDescription;
    private Instant createdDate;
    private Instant updateDate;
}
