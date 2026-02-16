package sk.tany.rest.api.domain.shopsettings;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface ShopSettingsRepository extends MongoRepository<ShopSettings, String> {
    default ShopSettings getFirstShopSettings() {
        return findAll().stream()
                .findFirst()
                .orElseGet(ShopSettings::new);
    }
}
