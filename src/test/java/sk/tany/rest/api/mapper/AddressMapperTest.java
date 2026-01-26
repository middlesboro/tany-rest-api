package sk.tany.rest.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.AddressDto;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AddressMapperTest {

    @Mock
    private ShopSettingsRepository shopSettingsRepository;

    private AddressMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AddressMapper() {
            @Override
            public Address toEntity(AddressDto dto) {
                return null; // Not needed for this test
            }
        };
        mapper.shopSettingsRepository = shopSettingsRepository;
    }

    @Test
    void toDto_whenAddressHasCountry_shouldUseIt() {
        Address address = new Address("Street", "City", "12345", "Slovakia");

        AddressDto dto = mapper.toDto(address);

        assertEquals("Street", dto.getStreet());
        assertEquals("City", dto.getCity());
        assertEquals("12345", dto.getZip());
        assertEquals("Slovakia", dto.getCountry());
    }

    @Test
    void toDto_whenAddressHasNoCountry_shouldUseDefault() {
        Address address = new Address("Street", "City", "12345", null);
        ShopSettings settings = new ShopSettings();
        settings.setDefaultCountry("Czech Republic");

        when(shopSettingsRepository.findAll()).thenReturn(Collections.singletonList(settings));

        AddressDto dto = mapper.toDto(address);

        assertEquals("Czech Republic", dto.getCountry());
    }

    @Test
    void toDto_whenAddressHasEmptyCountry_shouldUseDefault() {
        Address address = new Address("Street", "City", "12345", "");
        ShopSettings settings = new ShopSettings();
        settings.setDefaultCountry("Poland");

        when(shopSettingsRepository.findAll()).thenReturn(Collections.singletonList(settings));

        AddressDto dto = mapper.toDto(address);

        assertEquals("Poland", dto.getCountry());
    }

    @Test
    void toDto_whenAddressIsNull_shouldReturnNull() {
        assertNull(mapper.toDto(null));
    }
}
