package sk.tany.rest.api.domain.emailtemplate;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String> {
}
