package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.emailcampaign.EmailCampaignDto;
import sk.tany.rest.api.dto.admin.emailcampaign.patch.EmailCampaignPatchRequest;

import java.util.Optional;

public interface EmailCampaignAdminService {
    EmailCampaignDto save(EmailCampaignDto dto);
    Page<EmailCampaignDto> findAll(Pageable pageable);
    Optional<EmailCampaignDto> findById(String id);
    EmailCampaignDto update(String id, EmailCampaignDto dto);
    EmailCampaignDto patch(String id, EmailCampaignPatchRequest patch);
    void deleteById(String id);
}
