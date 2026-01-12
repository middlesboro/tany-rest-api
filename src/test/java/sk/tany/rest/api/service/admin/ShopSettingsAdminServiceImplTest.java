package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.impl.ShopSettingsAdminServiceImpl;
import sk.tany.rest.api.service.mapper.ShopSettingsAdminApiMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShopSettingsAdminServiceImplTest {

    @Mock
    private ShopSettingsRepository repository;

    @Mock
    private ShopSettingsAdminApiMapper mapper;

    @InjectMocks
    private ShopSettingsAdminServiceImpl service;

    @Test
    void update() {
        String id = "1";
        ShopSettingsUpdateRequest request = new ShopSettingsUpdateRequest();
        ShopSettings entity = new ShopSettings();
        entity.setId(id);
        ShopSettingsGetResponse response = new ShopSettingsGetResponse();
        response.setId(id);

        when(repository.findAll()).thenReturn(Collections.singletonList(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toGetResponse(entity)).thenReturn(response);

        ShopSettingsGetResponse result = service.update(request);

        assertEquals(id, result.getId());
        verify(mapper).update(entity, request);
        verify(repository).save(entity);
    }

    @Test
    void get() {
        String id = "1";
        ShopSettings entity = new ShopSettings();
        entity.setId(id);
        ShopSettingsGetResponse response = new ShopSettingsGetResponse();
        response.setId(id);

        when(repository.findAll()).thenReturn(Collections.singletonList(entity));
        when(mapper.toGetResponse(entity)).thenReturn(response);

        ShopSettingsGetResponse result = service.get();

        assertEquals(id, result.getId());
    }

    @Test
    void get_whenEmpty_createsNew() {
        ShopSettings newEntity = new ShopSettings();
        newEntity.setId("new");
        ShopSettingsGetResponse response = new ShopSettingsGetResponse();
        response.setId("new");

        when(repository.findAll()).thenReturn(Collections.emptyList());
        when(repository.save(any(ShopSettings.class))).thenReturn(newEntity);
        when(mapper.toGetResponse(newEntity)).thenReturn(response);

        ShopSettingsGetResponse result = service.get();

        assertEquals("new", result.getId());
        verify(repository).save(any(ShopSettings.class));
    }
}
