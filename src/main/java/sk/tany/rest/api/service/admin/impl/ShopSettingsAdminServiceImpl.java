package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.ShopSettingsAdminService;
import sk.tany.rest.api.service.mapper.ShopSettingsAdminApiMapper;

@Service
@RequiredArgsConstructor
public class ShopSettingsAdminServiceImpl implements ShopSettingsAdminService {

    private final ShopSettingsRepository repository;
    private final ShopSettingsAdminApiMapper mapper;

    private ShopSettings getOrCreateFirst() {
        return repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> repository.save(new ShopSettings()));
    }

    @Override

    public ShopSettingsGetResponse patch(sk.tany.rest.api.dto.admin.shopsettings.patch.ShopSettingsPatchRequest request) {
        ShopSettings entity = getOrCreateFirst();
        mapper.updateFromPatch(entity, request);
        entity = repository.save(entity);
        return mapper.toGetResponse(entity);
    }

    @Override

    public ShopSettingsGetResponse update(ShopSettingsUpdateRequest request) {
        ShopSettings entity = getOrCreateFirst();
        mapper.update(entity, request);
        entity = repository.save(entity);
        return mapper.toGetResponse(entity);
    }

    @Override
    public ShopSettingsGetResponse get() {
        return mapper.toGetResponse(getOrCreateFirst());
    }
}
