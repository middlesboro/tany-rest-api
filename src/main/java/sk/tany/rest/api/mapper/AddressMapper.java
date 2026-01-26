package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.AddressDto;

@Mapper(componentModel = "spring")
public abstract class AddressMapper {

    @Autowired
    protected ShopSettingsRepository shopSettingsRepository;

    private volatile String cachedDefaultCountry;
    private volatile long cacheTimestamp = 0;
    private static final long CACHE_DURATION = 60000; // 60 seconds

    public AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }

        AddressDto addressDto = new AddressDto();
        addressDto.setStreet(address.getStreet());
        addressDto.setCity(address.getCity());
        addressDto.setZip(address.getZip());
        addressDto.setCountry(address.getCountry());

        if (addressDto.getCountry() == null || addressDto.getCountry().isEmpty()) {
            addressDto.setCountry(getDefaultCountry());
        }

        return addressDto;
    }

    private String getDefaultCountry() {
        long now = System.currentTimeMillis();
        if (cachedDefaultCountry == null || (now - cacheTimestamp) > CACHE_DURATION) {
            synchronized (this) {
                if (cachedDefaultCountry == null || (now - cacheTimestamp) > CACHE_DURATION) {
                    shopSettingsRepository.findAll().stream().findFirst().ifPresent(shopSettings -> {
                        cachedDefaultCountry = shopSettings.getDefaultCountry();
                    });
                    cacheTimestamp = now;
                }
            }
        }
        return cachedDefaultCountry;
    }

    public abstract Address toEntity(AddressDto addressDto);
}
