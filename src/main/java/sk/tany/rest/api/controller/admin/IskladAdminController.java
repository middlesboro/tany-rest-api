package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.admin.InventoryDifferenceDto;
import sk.tany.rest.api.service.admin.IskladAdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/isklad")
@RequiredArgsConstructor
@Slf4j
public class IskladAdminController {

    private final IskladAdminService iskladAdminService;

    @GetMapping("/inventory-differences")
    public List<InventoryDifferenceDto> inventoryDifferences() {
        return iskladAdminService.getInventoryDifferences();
    }
}
