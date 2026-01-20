package sk.tany.rest.api.domain.shopsettings;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class ShopSettingsRepository extends AbstractInMemoryRepository<ShopSettings> {

    public ShopSettingsRepository(Nitrite nitrite) {
        super(nitrite, ShopSettings.class);
    }
}
