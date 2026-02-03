package sk.tany.rest.api.dto.admin.brand;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sk.tany.rest.api.dto.BrandDto;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrandAdminGetResponse extends BrandDto {
    private List<String> productIds;
}
