package sk.tany.rest.api.domain.shopsettings;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopSettingsRepository extends MongoRepository<ShopSettings, String> {
}
