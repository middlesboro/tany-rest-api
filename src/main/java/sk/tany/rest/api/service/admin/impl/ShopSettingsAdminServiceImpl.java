package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.admin.shopsettings.create.ShopSettingsCreateRequest;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.list.ShopSettingsListResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.ShopSettingsAdminService;
import sk.tany.rest.api.service.mapper.ShopSettingsAdminApiMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopSettingsAdminServiceImpl implements ShopSettingsAdminService {

    private final ShopSettingsRepository repository;
    private final ShopSettingsAdminApiMapper mapper;

    @Override
    @Transactional
    public ShopSettingsGetResponse create(ShopSettingsCreateRequest request) {
        ShopSettings entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toGetResponse(entity);
    }

    @Override
    @Transactional
    public ShopSettingsGetResponse update(String id, ShopSettingsUpdateRequest request) {
        ShopSettings entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ShopSettings not found"));
        mapper.update(entity, request);
        entity = repository.save(entity);
        return mapper.toGetResponse(entity);
    }

    @Override
    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public ShopSettingsGetResponse get(String id) {
        return repository.findById(id)
                .map(mapper::toGetResponse)
                .orElseThrow(() -> new RuntimeException("ShopSettings not found"));
    }

    @Override
    public List<ShopSettingsListResponse> list() {
        return mapper.toListResponse(repository.findAll());
    }
}
