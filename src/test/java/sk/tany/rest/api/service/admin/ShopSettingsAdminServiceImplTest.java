package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.admin.shopsettings.create.ShopSettingsCreateRequest;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.list.ShopSettingsListResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.impl.ShopSettingsAdminServiceImpl;
import sk.tany.rest.api.service.mapper.ShopSettingsAdminApiMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShopSettingsAdminServiceImplTest {

    @Mock
    private ShopSettingsRepository repository;

    @Mock
    private ShopSettingsAdminApiMapper mapper;

    @InjectMocks
    private ShopSettingsAdminServiceImpl service;

    @Test
    void create() {
        ShopSettingsCreateRequest request = new ShopSettingsCreateRequest();
        ShopSettings entity = new ShopSettings();
        ShopSettings savedEntity = new ShopSettings();
        savedEntity.setId("1");
        ShopSettingsGetResponse response = new ShopSettingsGetResponse();
        response.setId("1");

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toGetResponse(savedEntity)).thenReturn(response);

        ShopSettingsGetResponse result = service.create(request);

        assertEquals("1", result.getId());
        verify(repository).save(entity);
    }

    @Test
    void update() {
        String id = "1";
        ShopSettingsUpdateRequest request = new ShopSettingsUpdateRequest();
        ShopSettings entity = new ShopSettings();
        entity.setId(id);
        ShopSettingsGetResponse response = new ShopSettingsGetResponse();
        response.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toGetResponse(entity)).thenReturn(response);

        ShopSettingsGetResponse result = service.update(id, request);

        assertEquals(id, result.getId());
        verify(mapper).update(entity, request);
        verify(repository).save(entity);
    }

    @Test
    void delete() {
        String id = "1";
        service.delete(id);
        verify(repository).deleteById(id);
    }

    @Test
    void get() {
        String id = "1";
        ShopSettings entity = new ShopSettings();
        entity.setId(id);
        ShopSettingsGetResponse response = new ShopSettingsGetResponse();
        response.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toGetResponse(entity)).thenReturn(response);

        ShopSettingsGetResponse result = service.get(id);

        assertEquals(id, result.getId());
    }

    @Test
    void list() {
        ShopSettings entity = new ShopSettings();
        List<ShopSettings> entities = Collections.singletonList(entity);
        ShopSettingsListResponse response = new ShopSettingsListResponse();
        List<ShopSettingsListResponse> responses = Collections.singletonList(response);

        when(repository.findAll()).thenReturn(entities);
        when(mapper.toListResponse(entities)).thenReturn(responses);

        List<ShopSettingsListResponse> result = service.list();

        assertEquals(1, result.size());
    }
}
