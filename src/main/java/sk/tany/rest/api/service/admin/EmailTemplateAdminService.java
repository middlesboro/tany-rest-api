package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.emailtemplate.EmailTemplateDto;
import sk.tany.rest.api.dto.admin.emailtemplate.patch.EmailTemplatePatchRequest;

import java.util.Optional;

public interface EmailTemplateAdminService {
    EmailTemplateDto save(EmailTemplateDto dto);
    Page<EmailTemplateDto> findAll(Pageable pageable);
    Optional<EmailTemplateDto> findById(String id);
    EmailTemplateDto update(String id, EmailTemplateDto dto);
    EmailTemplateDto patch(String id, EmailTemplatePatchRequest patch);
    void deleteById(String id);
}
