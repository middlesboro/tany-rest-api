package sk.tany.rest.api.domain.emailcampaign;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailCampaignRepository extends MongoRepository<EmailCampaign, String> {
}
