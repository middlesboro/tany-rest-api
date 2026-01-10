package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.shopsettings.create.ShopSettingsCreateRequest;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.list.ShopSettingsListResponse;
import sk.tany.rest.api.dto.admin.shopsettings.patch.ShopSettingsPatchRequest;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.ShopSettingsAdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shop-settings")
@Tag(name = "Admin Shop Settings", description = "Endpoints for managing shop settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShopSettingsAdminController {

    private final ShopSettingsAdminService service;

    @GetMapping
    @Operation(summary = "List all shop settings")
    public List<ShopSettingsListResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create shop settings")
    public ShopSettingsGetResponse create(@RequestBody ShopSettingsCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shop settings by ID")
    public ShopSettingsGetResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update shop settings by ID")
    public ShopSettingsGetResponse update(@PathVariable String id, @RequestBody ShopSettingsUpdateRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch shop settings by ID")
    public ShopSettingsGetResponse patch(@PathVariable String id, @RequestBody ShopSettingsPatchRequest request) {
        return service.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete shop settings by ID")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
