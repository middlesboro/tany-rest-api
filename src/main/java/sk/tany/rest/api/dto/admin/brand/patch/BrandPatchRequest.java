package sk.tany.rest.api.dto.admin.brand.patch;

import lombok.Data;

@Data
public class BrandPatchRequest {
    private String name;
    private String image;
}
