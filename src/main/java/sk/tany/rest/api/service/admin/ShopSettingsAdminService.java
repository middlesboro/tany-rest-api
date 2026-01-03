package sk.tany.rest.api.service.admin;

import sk.tany.rest.api.dto.admin.shopsettings.create.ShopSettingsCreateRequest;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.list.ShopSettingsListResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;

import java.util.List;

public interface ShopSettingsAdminService {
    ShopSettingsGetResponse create(ShopSettingsCreateRequest request);

    ShopSettingsGetResponse update(String id, ShopSettingsUpdateRequest request);

    void delete(String id);

    ShopSettingsGetResponse get(String id);

    List<ShopSettingsListResponse> list();
}
