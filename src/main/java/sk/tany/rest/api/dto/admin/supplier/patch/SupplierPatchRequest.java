package sk.tany.rest.api.dto.admin.supplier.patch;

import lombok.Data;

@Data
public class SupplierPatchRequest {
    private Long prestashopId;
    private String name;
    private String metaTitle;
    private String metaDescription;
}
