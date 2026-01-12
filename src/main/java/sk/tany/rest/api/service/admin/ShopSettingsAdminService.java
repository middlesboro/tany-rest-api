package sk.tany.rest.api.service.admin;

import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;

public interface ShopSettingsAdminService {

    ShopSettingsGetResponse update(ShopSettingsUpdateRequest request);

    ShopSettingsGetResponse patch(sk.tany.rest.api.dto.admin.shopsettings.patch.ShopSettingsPatchRequest request);

    ShopSettingsGetResponse get();

}
