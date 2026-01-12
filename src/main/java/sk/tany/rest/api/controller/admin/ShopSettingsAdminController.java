package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.patch.ShopSettingsPatchRequest;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.ShopSettingsAdminService;

@RestController
@RequestMapping("/api/admin/shop-settings")
@Tag(name = "Admin Shop Settings", description = "Endpoints for managing shop settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShopSettingsAdminController {

    private final ShopSettingsAdminService service;

    @GetMapping
    @Operation(summary = "Get shop settings")
    public ShopSettingsGetResponse get() {
        return service.get();
    }

    @PutMapping
    @Operation(summary = "Update shop settings")
    public ShopSettingsGetResponse update(@RequestBody ShopSettingsUpdateRequest request) {
        return service.update(request);
    }

    @PatchMapping
    @Operation(summary = "Patch shop settings")
    public ShopSettingsGetResponse patch(@RequestBody ShopSettingsPatchRequest request) {
        return service.patch(request);
    }
}
