package sk.tany.rest.api.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.dto.admin.shopsettings.create.ShopSettingsCreateRequest;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.list.ShopSettingsListResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShopSettingsAdminApiMapper {

    ShopSettings toEntity(ShopSettingsCreateRequest request);

    ShopSettingsGetResponse toGetResponse(ShopSettings entity);

    ShopSettingsListResponse toListResponse(ShopSettings entity);

    List<ShopSettingsListResponse> toListResponse(List<ShopSettings> entities);

    void update(@MappingTarget ShopSettings entity, ShopSettingsUpdateRequest request);

    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromPatch(@MappingTarget ShopSettings entity, sk.tany.rest.api.dto.admin.shopsettings.patch.ShopSettingsPatchRequest patch);
}
