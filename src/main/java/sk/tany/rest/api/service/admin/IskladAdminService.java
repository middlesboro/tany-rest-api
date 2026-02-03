package sk.tany.rest.api.service.admin;

import sk.tany.rest.api.dto.admin.InventoryDifferenceDto;

import java.util.List;

public interface IskladAdminService {
    List<InventoryDifferenceDto> getInventoryDifferences();
}
