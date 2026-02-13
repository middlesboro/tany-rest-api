package sk.tany.rest.api.domain.onedrive;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface OneDriveTokenRepository extends MongoRepository<OneDriveToken, String> {}
